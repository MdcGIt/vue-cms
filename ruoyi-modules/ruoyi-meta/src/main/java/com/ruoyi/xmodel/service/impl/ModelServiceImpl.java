package com.ruoyi.xmodel.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.xmodel.XModelUtils;
import com.ruoyi.xmodel.db.DbTable;
import com.ruoyi.xmodel.db.DbTableColumn;
import com.ruoyi.xmodel.domain.XModel;
import com.ruoyi.xmodel.domain.XModelData;
import com.ruoyi.xmodel.domain.XModelField;
import com.ruoyi.xmodel.dto.XModelDTO;
import com.ruoyi.xmodel.exception.MetaErrorCode;
import com.ruoyi.xmodel.fixed.dict.MetaControlType;
import com.ruoyi.xmodel.mapper.XModelDataMapper;
import com.ruoyi.xmodel.mapper.XModelFieldMapper;
import com.ruoyi.xmodel.mapper.XModelMapper;
import com.ruoyi.xmodel.service.IModelService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModelServiceImpl extends ServiceImpl<XModelMapper, XModel> implements IModelService {

	private final XModelFieldMapper modelFieldMapper;
	
	private final XModelDataMapper modelDataMapepr;

	private final XModelMapper modelMapper;
	
	@Override
	public List<String> listModelDataCustomTable() {
		// 自定义表
		List<String> list = new ArrayList<>();
		// 默认表
		list.add(0, XModelUtils.DEFAULT_MODEL_VALUE_TABLE);
		this.modelMapper.listDbTableByPrefix(XModelUtils.CUSTOM_TABLE_NAME_PRFIX)
				.parallelStream().forEach(t -> list.add(t.getTableName()));
		return list;
	}

	@Override
	@Transactional
	public void addModel(XModelDTO dto) {
		if (StringUtils.isEmpty(dto.getTableName())) {
			dto.setTableName(XModelUtils.DEFAULT_MODEL_VALUE_TABLE);
		}
		if (!XModelUtils.isDefaultTable(dto.getTableName())) {
			// 非默认模型数据表需要校验
			long count = this.count(new LambdaQueryWrapper<XModel>().eq(XModel::getTableName, dto.getTableName()));
			Assert.isTrue(count == 0, () -> MetaErrorCode.META_TABLE_CONFICT.exception(dto.getTableName()));

			DbTable dbTable = this.modelMapper.getDbTable(dto.getTableName());
			Assert.notNull(dbTable, () -> MetaErrorCode.META_TABLE_NOT_EXISTS.exception(dto.getTableName()));
		}
		XModel model = new XModel();
		BeanUtils.copyProperties(dto, model, "modelId");
    	model.setModelId(IdUtils.getSnowflakeId());
    	model.createBy(dto.getOperator().getUsername());
    	this.save(model);
    	
		// 自定义表直接初始化字段
    	if (!XModelUtils.isDefaultTable(dto.getTableName())) {
			List<DbTableColumn> listTableColumn = this.modelMapper.listTableColumn(dto.getTableName(), XModelUtils.PRIMARY_FIELD_NAME);
			for (DbTableColumn column : listTableColumn) {
				XModelField field = new XModelField();
				field.setFieldId(IdUtils.getSnowflakeId());
				field.setModelId(model.getModelId());
				field.setName(StringUtils.isEmpty(column.getColumnComment()) ? column.getColumnName() : column.getColumnComment());
				field.setCode(column.getColumnName());
				field.setFieldName(column.getColumnName());
				field.setMandatoryFlag(column.isMandatory());
				field.setControlType(MetaControlType.INPUT);
				field.setDefaultValue(column.getColumnDefault());
				field.createBy(dto.getOperator().getUsername());
				this.modelFieldMapper.insert(field);
			}
    	}
	}

	@Override
	public void editModel(XModelDTO dto) {
		XModel model = this.getById(dto.getModelId());
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", dto.getModelId()));

		BeanUtils.copyProperties(dto, model, "modelId", "ownerType", "ownerId", "tableName");
		model.updateBy(dto.getOperator().getUsername());
		this.updateById(model);
	}
	
	@Override
	@Transactional
	public void deleteModel(List<Long> modelIds) {
		for (Long modelId : modelIds) {
			// TODO 校验模型是否被使用
			XModel model = this.getById(modelId);
			if (model != null) {
				// 移除模型字段数据
				this.modelFieldMapper.delete(new LambdaQueryWrapper<XModelField>().eq(XModelField::getModelId, modelId));
				// 删除模型数据表数据
				this.modelDataMapepr.delete(new LambdaQueryWrapper<XModelData>().eq(XModelData::getModelId, modelId));
				// 移除模型数据
				this.removeById(model.getModelId());
			}
		}
	}

	@Override
	public List<String> listModelTableFields(XModel model) {
		if (!model.getTableName().startsWith(XModelUtils.CUSTOM_TABLE_NAME_PRFIX)) {
			return new ArrayList<>();
		}
		List<DbTableColumn> listTableColumn = this.modelMapper.listTableColumn(model.getTableName(), XModelUtils.PRIMARY_FIELD_NAME);
		return listTableColumn.parallelStream().map(DbTableColumn::getColumnName).toList();
	}
}
