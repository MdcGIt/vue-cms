package com.ruoyi.contentcore.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;

@Component(IProperty.BEAN_NAME_PREFIX + PublishedContentEditProperty.ID)
public class PublishedContentEditProperty implements IProperty {

	public final static String ID = "PublishedContentEdit";
	
	static UseType[] UseTypes = new UseType[] { UseType.Site };
	
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
		return "是否允许编辑已发布内容";
	}
	
	@Override
	public String defaultValue() {
		return YesOrNo.YES;
	}
	
	public static String getValue(Map<String, String> props) {
		return ConfigPropertyUtils.getStringValue(ID, props);
	}
}
