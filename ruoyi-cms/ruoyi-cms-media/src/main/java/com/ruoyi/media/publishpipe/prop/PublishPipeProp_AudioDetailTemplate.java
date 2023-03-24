package com.ruoyi.media.publishpipe.prop;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IPublishPipeProp;
import com.ruoyi.media.AudioContentType;

@Component
public class PublishPipeProp_AudioDetailTemplate implements IPublishPipeProp {

	public static final String KEY = DetailTemplatePropPrefix + AudioContentType.ID;
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "音频集详情页模板";
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Catalog);
	}
}
