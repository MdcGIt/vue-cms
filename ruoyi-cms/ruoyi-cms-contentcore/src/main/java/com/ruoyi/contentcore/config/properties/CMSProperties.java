package com.ruoyi.contentcore.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "ruoyi.cms")
public class CMSProperties {
	
	/**
	 *	资源文件根目录 
	 */
	private String resourceRoot;
	
	/**
	 * 缓存名统一前缀
	 */
	private String cacheName = "cms:";
	
	/**
	 * 系统启动时是否清空cacheName前缀的所有缓存
	 */
	private Boolean resetCache = true;
}
