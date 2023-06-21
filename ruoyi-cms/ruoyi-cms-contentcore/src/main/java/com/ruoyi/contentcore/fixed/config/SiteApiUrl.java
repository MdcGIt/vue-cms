package com.ruoyi.contentcore.fixed.config;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.fixed.FixedConfig;
import com.ruoyi.system.service.ISysConfigService;
import org.springframework.stereotype.Component;

/**
 * 站点API域名地址
 */
@Component(FixedConfig.BEAN_PREFIX + SiteApiUrl.ID)
public class SiteApiUrl extends FixedConfig {

	public static final String ID = "SiteApiUrl";

	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);

	private static final String DEFAULT_VALUE = "http://localhost:8080/";

	public SiteApiUrl() {
		super(ID, "{CONFIG." + ID + "}", DEFAULT_VALUE, null);
	}
	
	public static String getValue() {
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isBlank(configValue)) {
			configValue = DEFAULT_VALUE;
		}
		if (!configValue.endsWith("/")) {
			configValue += "/";
		}
		return configValue;
	}
}
