package com.ruoyi.article.properties;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;

/**
 * 文章正文图片宽度，默认：600
 */
@Component(IProperty.BEAN_NAME_PREFIX + ArticleImageWidth.ID)
public class ArticleImageWidth implements IProperty {

	public final static String ID = "ArticleImageWidth";
	
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
		return "文章正文图片宽度";
	}
	
	@Override
	public boolean validate(String value) {
		return NumberUtils.isDigits(value.toString());
	}
	
	@Override
	public Integer defaultValue() {
		return 600;
	}
	
	@Override
	public Integer getPropValue(Map<String, String> configProps) {
		return MapUtils.getInteger(configProps, getId(), defaultValue());
	}
	
	public static int getValue(Map<String, String> firstProps, Map<String, String> secondProps) {
		int value = ConfigPropertyUtils.getIntValue(ID, firstProps, secondProps);
		return value <= 0 ? 600 : value;
	}
}
