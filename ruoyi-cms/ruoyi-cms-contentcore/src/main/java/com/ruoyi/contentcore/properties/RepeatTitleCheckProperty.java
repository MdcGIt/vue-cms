package com.ruoyi.contentcore.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;

/**
 * 校验重复标题策略
 */
@Component(IProperty.BEAN_NAME_PREFIX + RepeatTitleCheckProperty.ID)
public class RepeatTitleCheckProperty implements IProperty {

	public final static String ID = "RepeatTitleCheck";
	
	static UseType[] UseTypes = new UseType[] { UseType.Site, UseType.Catalog };

	public static final String CheckType_None = "0"; // 不校验
	public static final String CheckType_Site = "1"; // 全站校验
	public static final String CheckType_Catalog = "2"; // 栏目校验
	
	@Override
	public UseType[] getUseTypes() {
		return UseTypes;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "校验重复标题策略";
	}
	
	@Override
	public boolean validate(String value) {
		return StringUtils.isEmpty(value) || StringUtils.containsAny(value, CheckType_None, CheckType_Site, CheckType_Catalog);
	}
	
	@Override
	public String defaultValue() {
		return CheckType_None;
	}
	
	public static String getValue(Map<String, Object> firstConfigProps, Map<String, Object> secondConfigProps) {
		return ConfigPropertyUtils.getStringValue(ID, firstConfigProps, secondConfigProps);
	}
}
