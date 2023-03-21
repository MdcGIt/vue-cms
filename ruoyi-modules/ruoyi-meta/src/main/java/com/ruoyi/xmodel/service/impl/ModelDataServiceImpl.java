package com.ruoyi.xmodel.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.xmodel.XModelUtils;
import com.ruoyi.xmodel.domain.XModel;
import com.ruoyi.xmodel.domain.XModelData;
import com.ruoyi.xmodel.domain.XModelField;
import com.ruoyi.xmodel.mapper.XModelDataMapper;
import com.ruoyi.xmodel.mapper.XModelFieldMapper;
import com.ruoyi.xmodel.mapper.XModelMapper;
import com.ruoyi.xmodel.service.IModelDataService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModelDataServiceImpl extends ServiceImpl<XModelDataMapper, XModelData> implements IModelDataService {

	private final XModelMapper modelMapper;

	private final XModelFieldMapper modelFieldMapper;

	private final XModelDataMapper modelDataMapper;

	@Override
	public void saveModelData(Long modelId, String pkValue, Map<String, Object> params) {
		XModel model = this.modelMapper.selectById(modelId);
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", modelId));

		LambdaQueryWrapper<XModelField> q = new LambdaQueryWrapper<XModelField>().eq(XModelField::getModelId, modelId);
		List<XModelField> fields = this.modelFieldMapper.selectList(q);

		boolean insert = false;
		Map<String, Object> dataMap = null;
		if (XModelUtils.isDefaultTable(model.getTableName())) {
			dataMap = this.getMap(new LambdaQueryWrapper<XModelData>().eq(XModelData::getModelId, modelId)
					.eq(XModelData::getPkValue, pkValue));
			if (dataMap == null) {
				insert = true;
				dataMap = new HashMap<>();
				dataMap.put(XModelUtils.PRIMARY_FIELD_NAME, pkValue);
				dataMap.put("model_id", modelId);
				dataMap.put("data_id", IdUtils.getSnowflakeId());
			}
		} else {
			dataMap = this.modelDataMapper.getCustomModelData(model.getTableName(), pkValue);
			if (dataMap == null) {
				insert = true;
				dataMap = new HashMap<>();
				dataMap.put(XModelUtils.PRIMARY_FIELD_NAME, pkValue);
			}
		}

		for (XModelField field : fields) {
			Object fieldValue = params.get(field.getFieldName());
			if (fieldValue == null) {
				fieldValue = field.getDefaultValue();
			} else if (fieldValue.getClass().isArray()) {
				Object[] arr = (Object[]) fieldValue;
				fieldValue = StringUtils.join(arr, StringUtils.COMMA);
			} else if (fieldValue instanceof List<?> list) {
				fieldValue = StringUtils.join(list, StringUtils.COMMA);
			}
			dataMap.put(field.getFieldName(), fieldValue);
		}
		if (insert) {
			StringBuilder sbInsertFields = new StringBuilder();
			StringBuilder sbInsertValues = new StringBuilder();
			dataMap.entrySet().forEach(e -> {
				sbInsertFields.append(sbInsertFields.length() > 0 ? "," : "").append(e.getKey());
				sbInsertValues.append(sbInsertValues.length() > 0 ? "," : "");
				if (e.getValue() == null) {
					sbInsertValues.append("null");
				} else {
					sbInsertValues.append("'").append(e.getValue()).append("'");
				}
			});
			this.modelDataMapper.insertCustomModelData(model.getTableName(), sbInsertFields.toString(),
					sbInsertValues.toString());
		} else {
			StringBuilder sbUpdateFields = new StringBuilder();
			dataMap.entrySet().forEach(e -> {
				sbUpdateFields.append(sbUpdateFields.length() > 0 ? "," : "").append(e.getKey()).append("=");
				if (e.getValue() == null) {
					sbUpdateFields.append("null");
				} else {
					sbUpdateFields.append("'").append(e.getValue()).append("'");
				}
			});
			this.modelDataMapper.udpateCustomModelData(model.getTableName(), sbUpdateFields.toString(), pkValue);
		}
	}

	@Override
	public Map<String, Object> getModelData(XModel model, String pkValue) {
		if (StringUtils.isNotEmpty(pkValue)) {
			if (XModelUtils.isDefaultTable(model.getTableName())) {
				Map<String, Object> data = this.getMap(new LambdaQueryWrapper<XModelData>()
						.eq(XModelData::getModelId, model.getModelId()).eq(XModelData::getPkValue, pkValue));
				if (data != null) {
					return data;
				}
			} else {
				Map<String, Object> data = this.modelDataMapper.getCustomModelData(model.getTableName(), pkValue);
				if (data != null) {
					return data;
				}
			}
		}
		return Collections.emptyMap();
	}

	@Override
	public void deleteModelData(Long modeId, String pkValue) {
		XModel model = this.modelMapper.selectById(modeId);
		if (XModelUtils.isDefaultTable(model.getTableName())) {
			this.remove(this.lambdaQuery().eq(XModelData::getModelId, modeId).eq(XModelData::getPkValue, pkValue));
		} else {
			this.modelDataMapper.deleteCustomModelData(model.getTableName(), pkValue);
		}
	}
}
