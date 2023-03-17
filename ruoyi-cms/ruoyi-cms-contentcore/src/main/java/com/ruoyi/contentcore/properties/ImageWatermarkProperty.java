package com.ruoyi.contentcore.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;

/**
 * 是否开启图片水印
 */
@Component(IProperty.BEAN_NAME_PREFIX + ImageWatermarkProperty.ID)
public class ImageWatermarkProperty implements IProperty {

	public final static String ID = "ImageWatermark";

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
		return "图片水印";
	}

	@Override
	public boolean validate(String value) {
		return StringUtils.isEmpty(value) || YesOrNo.YES.equals(value) || YesOrNo.NO.equals(value);
	}

	@Override
	public String defaultValue() {
		return YesOrNo.NO;
	}
	
	public static boolean getValue(Map<String, Object> props) {
		return YesOrNo.isYes(ConfigPropertyUtils.getStringValue(ID, props));
	}
}
