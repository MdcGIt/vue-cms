package com.ruoyi.article.properties;

import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;

/**
 * 文章正文图片高度，默认：600
 */
@Component(IProperty.BEAN_NAME_PREFIX + ArticleImageHeight.ID)
public class ArticleImageHeight implements IProperty {

	public final static String ID = "ArticleImageHeight";
	
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
		return "文章正文图片高度";
	}
	
	@Override
	public boolean validate(String value) {
		return NumberUtils.isDigits(value);
	}
	
	@Override
	public String defaultValue() {
		return "0";
	}
	
	public static int getValue(Map<String, Object> firstProps, Map<String, Object> secondProps) {
		return ConfigPropertyUtils.getIntValue(ID, firstProps, secondProps);
	}
}
