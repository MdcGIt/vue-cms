package com.ruoyi.exmodel.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;

/**
 * 关联扩展模型
 */
@Component(IProperty.BEAN_NAME_PREFIX + ExtendModelProperty.ID)
public class ExtendModelProperty implements IProperty {

	public final static String ID = "ExtendModel";
	
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
		return "扩展模型";
	}
	
	public static String getValue(Map<String, Object> props) {
		return ConfigPropertyUtils.getStringValue(ID, props);
	}
}
