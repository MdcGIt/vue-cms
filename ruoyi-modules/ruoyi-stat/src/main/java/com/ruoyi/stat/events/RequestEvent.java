package com.ruoyi.stat.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class RequestEvent {
	
	/**
	 * 请求域
	 */
	private String host;
	
	/**
	 * IP地址
	 */
	private String ipAddress;
	
	/**
	 * IP所属地区
	 */
	private String address;
	
	/**
	 * 来源地址
	 */
	private String referer;
	
	/**
	 * 浏览器类型
	 */
	private String browser;
	
	/**
	 * UserAgent
	 */
	private String userAgent;
	
	/**
	 * 操作系统
	 */
	private String os;
	
	/**
	 * 语言
	 */
	private String locale;
	
	/**
	 * 屏幕分辨率
	 */
	private String resolution;
	
	/**
	 * 事件时间
	 */
	private Long timestamp;
}
