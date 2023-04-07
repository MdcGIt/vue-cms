package com.ruoyi.system.fixed.config;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.exception.SysErrorCode;
import com.ruoyi.system.fixed.FixedConfig;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 系统模块通用文件上传类型限制
 */
@Component(FixedConfig.BEAN_PREFIX + SysUploadTypeLimit.ID)
public class SysUploadTypeLimit extends FixedConfig {

	public static final String ID = "SysUploadTypeLimit";
	
	private static final ISysConfigService configService = SpringUtils.getBean(ISysConfigService.class);
	
	/**
	 * 默认上传文件类型
	 */
	private static final String[] DEFAULT_VALUE = { "jpg", "jpeg", "gif", "png", "xls", "xlsx", "doc", "docx", "ppt", "pptx", "pdf", "html", "txt", "zip" };
	
	public SysUploadTypeLimit() {
		super(ID, "{CONFIG." + ID + "}", String.join(",", DEFAULT_VALUE), null);
	}
	
	public static void check(String ext) {
		boolean flag;
		String configValue = configService.selectConfigByKey(ID);
		if (StringUtils.isEmpty(configValue)) {
			flag = StringUtils.containsAnyIgnoreCase(ext, DEFAULT_VALUE);
		} else {
			flag = StringUtils.containsAnyIgnoreCase(ext, configValue.split(","));
		}
		Assert.isTrue(flag, () -> SysErrorCode.UPLOAD_FILE_TYPE_LIMIT.exception());
	}
}
