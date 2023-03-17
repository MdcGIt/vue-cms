package com.ruoyi.system.fixed.config;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.fixed.FixedConfig;
import com.ruoyi.system.service.ISysConfigService;

@Component(FixedConfig.BEAN_PREFIX + SysCaptchaEnable.ID)
public class SysCaptchaEnable extends FixedConfig {

	public static final String ID = "SysCaptchaEnable";
	
	private static final String DEFAULT_VALUE = "true";
	
	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);
	
	public SysCaptchaEnable() {
		super(ID, "CONFIG." + ID, DEFAULT_VALUE, null);
	}
	
	public static boolean isEnable() {
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isEmpty(configValue)) {
			return true;
		}
		return Boolean.parseBoolean(configValue);
	}
}
