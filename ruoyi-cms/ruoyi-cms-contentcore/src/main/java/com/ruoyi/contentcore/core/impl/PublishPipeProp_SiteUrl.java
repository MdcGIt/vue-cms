package com.ruoyi.contentcore.core.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IPublishPipeProp;

@Component
public class PublishPipeProp_SiteUrl implements IPublishPipeProp {
	
	public static final String KEY = "url";

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "访问域名";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Site);
	}

	public static String getValue(String publishPipeCode, Map<String, Map<String, Object>> publishPipeProps) {
		if (Objects.nonNull(publishPipeProps)) {
			return MapUtils.getString(publishPipeProps.get(publishPipeCode), KEY);
		}
		return null;
	}
}
