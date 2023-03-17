package com.ruoyi.cms.image;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IPublishPipeProp;

@Component
public class PublishPipeProp_DefaultImageDetailTemplate implements IPublishPipeProp {

	public static final String KEY = DefaultDetailTemplatePropPrefix + ImageContentType.ID;
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "图片集详情页默认模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Site);
	}
}
