package com.ruoyi.xmodel.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ruoyi.common.mybatisplus.service.IDBService;
import com.ruoyi.xmodel.core.impl.MetaControlType_Checkbox;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysDictTypeService;
import com.ruoyi.xmodel.XModelUtils;
import com.ruoyi.xmodel.domain.XModel;
import com.ruoyi.xmodel.domain.XModelField;
import com.ruoyi.xmodel.dto.FieldOptions;
import com.ruoyi.xmodel.dto.XModelFieldDTO;
import com.ruoyi.xmodel.dto.XModelFieldDataDTO;
import com.ruoyi.xmodel.exception.MetaErrorCode;
import com.ruoyi.xmodel.fixed.dict.MetaFieldType;
import com.ruoyi.xmodel.mapper.XModelFieldMapper;
import com.ruoyi.xmodel.mapper.XModelMapper;
import com.ruoyi.xmodel.service.IModelDataService;
import com.ruoyi.xmodel.service.IModelFieldService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModelFieldServiceImpl extends ServiceImpl<XModelFieldMapper, XModelField> implements IModelFieldService {

	private final XModelMapper modelMapper;

	private final IModelDataService modelDataService;

	private final ISysDictTypeService dictService;

	private final IDBService dbService;

	@Override
	public void addModelField(XModelFieldDTO dto) {
		XModel model = this.modelMapper.selectById(dto.getModelId());
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", dto.getModelId()));

		dto.setFieldId(null);
		if (!this.checkFieldCodeUnique(dto)) {
			throw CommonErrorCode.DATA_CONFLICT.exception("code");
		}
		boolean isDefaultTable = XModelUtils.isDefaultTable(model.getTableName());
		String[] usedFields = this.getUsedFields(model.getModelId(), dto.getFieldType(), isDefaultTable);
		if (isDefaultTable) {
			int fieldTypeLimit = MetaFieldType.getFieldTypeLimit(dto.getFieldType());
			Assert.isTrue(fieldTypeLimit > usedFields.length,
					() -> MetaErrorCode.FIELD_LIMIT.exception(dto.getFieldType()));

			for (int i = 1; i <= fieldTypeLimit; i++) {
				if (!StringUtils.containsAny(dto.getFieldType() + i, usedFields)) {
					dto.setFieldName(dto.getFieldType() + i);
					break;
				}
			}
		} else {
			if (StringUtils.containsAny(dto.getFieldName(), usedFields)) {
				throw MetaErrorCode.META_FIELD_CONFLICT.exception(dto.getFieldName());
			}
			if (!isTableContainsColumn(model.getTableName(), dto.getFieldName())) {
				throw MetaErrorCode.DB_FIELD_NOT_EXISTS.exception(dto.getFieldName());
			}
		}
		XModelField xModelField = new XModelField();
		BeanUtils.copyProperties(dto, xModelField, "fieldId");
		xModelField.setFieldId(IdUtils.getSnowflakeId());
		xModelField.createBy(dto.getOperator().getUsername());
		this.save(xModelField);
	}

	@Override
	public void editModelField(XModelFieldDTO dto) {
		XModelField modelField = this.getById(dto.getFieldId());
		Assert.notNull(modelField, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("fieldId", dto.getFieldId()));

		if (!this.checkFieldCodeUnique(dto)) {
			throw CommonErrorCode.DATA_CONFLICT.exception("code");
		}
		String oldFieldName = modelField.getFieldName();
		String oldFieldType = modelField.getFieldType();

		XModel model = this.modelMapper.selectById(dto.getModelId());
		boolean isDefaultTable = XModelUtils.isDefaultTable(model.getTableName());
		if (isDefaultTable && !dto.getFieldType().equals(oldFieldType)) {
			// 字段种类变更，重新计算是否有可用字段
			String[] usedFields = this.getUsedFields(model.getModelId(), dto.getFieldType(), true);
			int fieldTypeLimit = MetaFieldType.getFieldTypeLimit(dto.getFieldType());
			Assert.isTrue(fieldTypeLimit > usedFields.length,
					() -> MetaErrorCode.FIELD_LIMIT.exception(dto.getFieldType()));

			for (int i = 1; i <= fieldTypeLimit; i++) {
				if (!StringUtils.containsAny(dto.getFieldType() + i, usedFields)) {
					dto.setFieldName(dto.getFieldType() + i);
					break;
				}
			}
		} else if (!isDefaultTable && !dto.getFieldName().equals(oldFieldName)) {
			String[] usedFields = this.getUsedFields(model.getModelId(), dto.getFieldType(), false);
			if (StringUtils.containsAny(dto.getFieldName(), usedFields)) {
				throw MetaErrorCode.META_FIELD_CONFLICT.exception(dto.getFieldName());
			}
			if (!isTableContainsColumn(model.getTableName(), dto.getFieldName())) {
				throw MetaErrorCode.DB_FIELD_NOT_EXISTS.exception(dto.getFieldName());
			}
		}
		BeanUtils.copyProperties(dto, modelField, "fieldId", "modelId");
		modelField.updateBy(dto.getOperator().getUsername());
		this.updateById(modelField);
	}

	@Override
	public void deleteModelField(List<Long> fieldIds) {
		this.removeByIds(fieldIds);
	}

	/**
	 * 判断指定表`tableName`是否含有指定字段`columnName`
	 *
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	private boolean isTableContainsColumn(String tableName, String columnName) {
		return this.dbService.getDbType().listTableColumns(tableName)
				.stream().filter(tc -> tc.getName().equals(columnName)).findFirst().isPresent();
	}

	/**
	 * 获取指定模型已配置过的数据库字段名
	 *
	 * @param modelId
	 * @param fieldType
	 * @param isDefaultTable
	 * @return
	 */
	private String[] getUsedFields(Long modelId, String fieldType, boolean isDefaultTable) {
		LambdaQueryWrapper<XModelField> q = new LambdaQueryWrapper<XModelField>()
				.eq(XModelField::getModelId, modelId)
				.eq(isDefaultTable, XModelField::getFieldType, fieldType);
		List<XModelField> list = this.list(q);
		return list.stream().map(f -> f.getFieldName()).toArray(String[]::new);
	}

	/**
	 * 校验字段编码是否已存在
	 */
	private boolean checkFieldCodeUnique(XModelFieldDTO dto) {
		LambdaQueryWrapper<XModelField> q = new LambdaQueryWrapper<XModelField>()
				.eq(XModelField::getModelId, dto.getModelId())
				.eq(XModelField::getCode, dto.getCode())
				.ne(dto.getFieldId() != null && dto.getFieldId() > 0, XModelField::getFieldId, dto.getFieldId());
		return this.count(q) == 0;
	}

	@Override
	public List<XModelFieldDataDTO> getFieldDatas(Long modelId, String pkValue) {
		XModel model = this.modelMapper.selectById(modelId);
		List<XModelField> list = this.list(new LambdaQueryWrapper<XModelField>()
				.eq(XModelField::getModelId, modelId));
		Map<String, Object> modelData = this.modelDataService.getModelData(model, pkValue);
		List<XModelFieldDataDTO> result = list.stream().map(f -> {
			XModelFieldDataDTO dto = XModelFieldDataDTO.newInstance(f, MapUtils.getString(modelData, f.getFieldName(), f.getDefaultValue()));
			dto.setOptions(getOptions(f.getOptions()));
			if (MetaControlType_Checkbox.TYPE.equals(dto.getControlType())) {
				if (dto.getValue() == null || StringUtils.isBlank(dto.getValue().toString())) {
					dto.setValue(new String[0]);
				} else {
					String[] value = StringUtils.split(dto.getValue().toString(), StringUtils.COMMA);
					dto.setValue(value);
				}
			}
			return dto;
		}).toList();
		return result;
	}

	private List<Map<String, String>> getOptions(FieldOptions options) {
		List<Map<String, String>> list = new ArrayList<>();
		if (options != null && StringUtils.isNotEmpty(options.getValue())) {
			if (XModelUtils.OPTIONS_TYPE_DICT.equals(options.getType())) {
				return this.dictService.selectDictDatasByType(options.getValue())
						.stream().map(dd -> Map.of("value", dd.getDictValue(), "name", dd.getDictLabel()))
						.toList();
			} else if(XModelUtils.OPTIONS_TYPE_TEXT.equals(options.getType())) {
				String[] split = options.getValue().split("\n");
				for (String string : split) {
					if (string.indexOf("=") > -1) {
						String value = StringUtils.substringBefore(string, "=");
						String name = StringUtils.substringAfter(string, "=");
						list.add(Map.of("value", value, "name", name));
					}
				}
			}
		}
		return list;
	}
}
