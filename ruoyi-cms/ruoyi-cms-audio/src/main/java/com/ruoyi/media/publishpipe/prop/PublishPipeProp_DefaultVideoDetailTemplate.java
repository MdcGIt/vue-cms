package com.ruoyi.media.publishpipe.prop;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IPublishPipeProp;
import com.ruoyi.media.VideoContentType;

@Component
public class PublishPipeProp_DefaultVideoDetailTemplate implements IPublishPipeProp {

	public static final String KEY = DefaultDetailTemplatePropPrefix + VideoContentType.ID;
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "视频集详情页默认模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Site);
	}
}
