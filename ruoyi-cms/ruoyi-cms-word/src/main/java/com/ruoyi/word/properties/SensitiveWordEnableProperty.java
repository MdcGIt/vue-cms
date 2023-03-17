package com.ruoyi.word.properties;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IProperty;

/**
 * 敏感词开关
 */
@Component(IProperty.BEAN_NAME_PREFIX + SensitiveWordEnableProperty.ID)
public class SensitiveWordEnableProperty implements IProperty {

	public final static String ID = "SensitiveWordEnable";
	
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
		return "是否开启敏感词";
	}
}
