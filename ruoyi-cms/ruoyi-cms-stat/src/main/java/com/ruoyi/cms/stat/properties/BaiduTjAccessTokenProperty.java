package com.ruoyi.cms.stat.properties;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;

@Component(IProperty.BEAN_NAME_PREFIX + BaiduTjAccessTokenProperty.ID)
public class BaiduTjAccessTokenProperty implements IProperty {

	public final static String ID = "BaiduTjAccessToken";

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
		return "百度统计AccessToken";
	}
	
	@Override
	public boolean isSensitive() {
		return true;
	}

	public static String getValue(Map<String, String> siteConfigProps) {
		return ConfigPropertyUtils.getStringValue(ID, siteConfigProps);
	}
}
