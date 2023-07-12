package com.ruoyi.xmodel.util;

import com.ruoyi.common.exception.GlobalException;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysDictTypeService;
import com.ruoyi.xmodel.core.IMetaModelType;
import com.ruoyi.xmodel.core.MetaModelField;
import com.ruoyi.xmodel.dto.FieldOptions;
import com.ruoyi.xmodel.exception.MetaErrorCode;

import java.util.*;

public class XModelUtils {

	/**
	 * 字段选择项类型：文本
	 * <p>格式：</p>
	 */
	public final static String OPTIONS_TYPE_TEXT = "text";

	/**
	 * 字段选择项类型：字典数据
	 */
	public final static String OPTIONS_TYPE_DICT = "dict";

	private static final Map<String, IMetaModelType> metaModelTypeMap = SpringUtils.getBeanMap(IMetaModelType.class);

	private static final ISysDictTypeService dictService = SpringUtils.getBean(ISysDictTypeService.class);

	public static IMetaModelType getMetaModelType(String type) {
		IMetaModelType iMetaModelType = metaModelTypeMap.get(IMetaModelType.BEAN_PREFIX + type);
		Assert.notNull(iMetaModelType, () -> MetaErrorCode.UNSUPPORTED_META_MODEL_TYPE.exception(type));
		return iMetaModelType;
	}

	public static void validateMetaModelTypes() {
		for (Iterator<IMetaModelType> iterator = metaModelTypeMap.values().iterator();iterator.hasNext();) {
			IMetaModelType mmt = iterator.next();
			Optional<MetaModelField> opt = mmt.getFixedFields().stream()
					.filter(f -> f.getFieldName().equals(IMetaModelType.MODEL_ID_FIELD_NAME))
					.findFirst();
			Assert.isTrue(opt.isPresent(), () -> new RuntimeException("IMetaModelType[" + mmt.getType() + "] missing mandatory field `model_id`."));
		}
	}

	public static List<Map<String, String>> getOptions(FieldOptions options) {
		List<Map<String, String>> list = new ArrayList<>();
		if (options != null && StringUtils.isNotEmpty(options.getValue())) {
			if (XModelUtils.OPTIONS_TYPE_DICT.equals(options.getType())) {
				return dictService.selectDictDatasByType(options.getValue())
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
