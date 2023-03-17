package com.ruoyi.word.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;

/**
 * 应用热词分组IDs
 */
@Component(IProperty.BEAN_NAME_PREFIX + HotWordGroupsProperty.ID)
public class HotWordGroupsProperty implements IProperty {

	public final static String ID = "HotWordGroups";
	
	static UseType[] UseTypes = new UseType[] { UseType.Site, UseType.Catalog };
	
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
		return "热词分组";
	}
	
	@Override
	public Class<?> valueClass() {
		return Long[].class;
	}
	
	public static Long[] getHotWordGroupIds(Map<String, Object> firstProps, Map<String, Object> secondProps) {
		String propValue = ConfigPropertyUtils.getStringValue(ID, firstProps, secondProps);
		return JacksonUtils.from(propValue, Long[].class);
	}
}
