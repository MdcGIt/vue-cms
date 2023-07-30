package com.ruoyi.member.fixed.config;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.fixed.config.BackendContext;
import com.ruoyi.member.config.MemberConfig;
import com.ruoyi.system.fixed.FixedConfig;
import com.ruoyi.system.service.ISysConfigService;
import org.springframework.stereotype.Component;

@Component(FixedConfig.BEAN_PREFIX + MemberResourcePrefix.ID)
public class MemberResourcePrefix extends FixedConfig {

	public static final String ID = "MemberResourcePrefix";

	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);

	public MemberResourcePrefix() {
		super(ID, "{CONFIG." + ID + "}", "", null);
	}

	public static String getValue() {
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isEmpty(configValue)) {
			return BackendContext.getValue() + MemberConfig.getResourcePrefix();
		}
		if (!configValue.endsWith("/")) {
			configValue += "/";
		}
		return configValue;
	}
}
