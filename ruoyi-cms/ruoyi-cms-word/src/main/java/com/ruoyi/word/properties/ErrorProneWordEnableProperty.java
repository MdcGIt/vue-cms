package com.ruoyi.word.properties;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IProperty;

/**
 * 易错词开关
 */
@Component(IProperty.BEAN_NAME_PREFIX + ErrorProneWordEnableProperty.ID)
public class ErrorProneWordEnableProperty implements IProperty {

	public final static String ID = "ErrorProneWordEnable";
	
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
		return "是否开启易错词";
	}
}
