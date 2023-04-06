package com.ruoyi.cms.stat.baidu.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaiduSite {

	/**
	 * 站点ID
	 */
	private Long site_id;

	/**
	 * 站点域名
	 */
	private String domain;

	/**
	 * 状态（0=正常，1=停用）
	 */
	private Integer status;

	/**
	 * 创建时间
	 */
	private LocalDateTime create_time;
}