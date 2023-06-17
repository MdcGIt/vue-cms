package com.ruoyi.xmodel.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.common.mybatisplus.db.DBTable;
import com.ruoyi.common.mybatisplus.db.DBTableColumn;
import com.ruoyi.common.mybatisplus.db.IDbType;
import com.ruoyi.common.mybatisplus.service.IDBService;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.system.fixed.dict.YesOrNo;
import com.ruoyi.xmodel.core.IMetaModelType;
import com.ruoyi.xmodel.core.MetaModel;
import com.ruoyi.xmodel.core.MetaModelField;
import com.ruoyi.xmodel.core.impl.MetaControlType_Input;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.xmodel.util.XModelUtils;
import com.ruoyi.xmodel.domain.XModel;
import com.ruoyi.xmodel.domain.XModelField;
import com.ruoyi.xmodel.dto.XModelDTO;
import com.ruoyi.xmodel.exception.MetaErrorCode;
import com.ruoyi.xmodel.mapper.XModelFieldMapper;
import com.ruoyi.xmodel.mapper.XModelMapper;
import com.ruoyi.xmodel.service.IModelService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModelServiceImpl extends ServiceImpl<XModelMapper, XModel> implements IModelService {

	private static final String CACHE_PREFIX = "xmodel:";

	private final XModelFieldMapper modelFieldMapper;

	private final IDBService dbService;

	private final RedisCache redisCache;

	@Override
	public MetaModel getMetaModel(Long modelId) {
		MetaModel model = this.redisCache.getCacheObject(CACHE_PREFIX + modelId, () -> {
			XModel xmodel = getById(modelId);
			if (xmodel == null) {
				return null;
			}

			List<MetaModelField> fields = new LambdaQueryChainWrapper<>(modelFieldMapper)
					.eq(XModelField::getModelId, modelId).list()
					.stream().map(f -> new MetaModelField(f))
					.toList();
			MetaModel metaModel = new MetaModel();
			metaModel.setModel(xmodel);
			metaModel.setFields(fields);
			return metaModel;
		});
		Assert.notNull(model, () -> MetaErrorCode.META_MODEL_NOT_FOUND.exception(modelId));
		return model;
	}

	@Override
	public void clearMetaModelCache(Long modelId) {
		this.redisCache.deleteObject(CACHE_PREFIX + modelId);
	}

	@Override
	public List<String> listModelDataTables(String type) {
		IMetaModelType mmt = XModelUtils.getMetaModelType(type);
		List<String> list = new ArrayList<>();
		// 数据表
		this.dbService.listTables(null).forEach(t -> {
			if (t.getTableName().startsWith(mmt.getTableNamePrefix())) {
				list.add(t.getTableName());
			}
		});
		return list;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addModel(XModelDTO dto) {
		IMetaModelType mmt = XModelUtils.getMetaModelType(dto.getOwnerType());
		if (StringUtils.isEmpty(dto.getTableName())) {
			dto.setTableName(mmt.getDefaultTable());
		}
		if (!mmt.getDefaultTable().equals(dto.getTableName())) {
			// 非默认模型数据表需要校验
			long count = this.count(new LambdaQueryWrapper<XModel>().eq(XModel::getTableName, dto.getTableName()));
			Assert.isTrue(count == 0, () -> MetaErrorCode.META_TABLE_CONFICT.exception(dto.getTableName()));

			List<DBTable> dbTables = this.dbService.listTables(dto.getTableName());
			Assert.isFalse(dbTables.isEmpty(), () -> MetaErrorCode.META_TABLE_NOT_EXISTS.exception(dto.getTableName()));
		}
		XModel model = new XModel();
		BeanUtils.copyProperties(dto, model, "modelId");
		model.setModelId(IdUtils.getSnowflakeId());
		model.createBy(dto.getOperator().getUsername());
		this.save(model);

		List<String> fixedFields = mmt.getFixedFields().stream().map(f -> f.getFieldName()).toList();
		// 自定义表直接初始化非固定字段
		if (!mmt.getDefaultTable().equals(dto.getTableName())) {
			IDbType dbType = this.dbService.getDbType();
			List<DBTableColumn> listTableColumn = dbType.listTableColumns(dto.getTableName());
			for (DBTableColumn column : listTableColumn) {
				if (!fixedFields.contains(column.getName())) {
					XModelField field = new XModelField();
					field.setFieldId(IdUtils.getSnowflakeId());
					field.setModelId(model.getModelId());
					field.setName(StringUtils.isEmpty(column.getColumnComment()) ? column.getName() : column.getColumnComment());
					field.setCode(column.getName());
					field.setFieldName(column.getName());
					field.setMandatoryFlag(column.isNullable() ? YesOrNo.NO : YesOrNo.YES);
					field.setControlType(MetaControlType_Input.TYPE);
					field.setDefaultValue(column.getDefaultValue());
					field.createBy(dto.getOperator().getUsername());
					this.modelFieldMapper.insert(field);
				}
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
		this.clearMetaModelCache(model.getModelId());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteModel(List<Long> modelIds) {
		for (Long modelId : modelIds) {
			XModel model = this.getById(modelId);
			if (model != null) {
				// 移除模型字段数据
				this.modelFieldMapper.delete(new LambdaQueryWrapper<XModelField>().eq(XModelField::getModelId, modelId));
				// 删除模型数据表数据
				this.dbService.getDbType().dropTable(model.getTableName());
				// 移除模型数据
				this.removeById(model.getModelId());
				// 清理缓存
				this.clearMetaModelCache(model.getModelId());
			}
		}
	}

	@Override
	public List<String> listModelTableFields(XModel model) {
		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getOwnerType());
		if (model.getTableName().equals(mmt.getDefaultTable())) {
			return new ArrayList<>();
		}
		return this.dbService.listTableColumns(model.getTableName())
				.stream()
				.map(DBTableColumn::getName)
				.toList();
	}
}
