package com.ruoyi.cms.member.publishpipe;

import com.ruoyi.contentcore.core.IPublishPipeProp;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PublishPipeProp_MemberLoginTemplate implements IPublishPipeProp {

	public static final String KEY = "memberLoginTemplate";

	static final String DEFAULT_VALUE = "account/login.template.html";
	
	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return "会员登录页模板";
	}

	@Override
	public String getDefaultValue() {
		return DEFAULT_VALUE;
	}

	@Override
	public List<PublishPipePropUseType> getUseTypes() {
		return List.of(PublishPipePropUseType.Site);
	}

	public static String getValue(String publishPipeCode, Map<String, Map<String, Object>> publishPipeProps) {
		if (Objects.nonNull(publishPipeProps)) {
			return MapUtils.getString(publishPipeProps.get(publishPipeCode), KEY, DEFAULT_VALUE);
		}
		return DEFAULT_VALUE;
	}
}
