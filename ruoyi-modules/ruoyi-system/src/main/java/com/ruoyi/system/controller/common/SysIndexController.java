package com.ruoyi.system.controller.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.config.properties.RuoYiProperties;
import com.ruoyi.common.utils.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 * 首页
 *
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
public class SysIndexController {
	
	private final RuoYiProperties properties;

	/**
	 * 访问首页，提示语
	 */
	@RequestMapping("/")
	public String index() {
		return StringUtils.messageFormat("欢迎使用{0}，当前版本：v{1}，请通过前端地址访问。", properties.getName(), properties.getVersion());
	}
}
