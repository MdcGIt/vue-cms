package com.ruoyi.cms.stat.baidu.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaiduTimeTrendDTO {

	/**
	 * 站点ID
	 */
	@NotNull
	private Long siteId;
	
	/**
	 * 开始时间
	 */
	@NotNull
	private LocalDateTime startDate;
	
	/**
	 * 结束时间
	 */
	@NotNull
	private LocalDateTime endDate;
	
	/**
	 * 指标
	 */
	private List<String> metrics;
	
	/**
	 * 时间粒度
	 */
	@NotEmpty
	private String gran;
	
	/**
	 * 来源
	 */
	private String source;
	
	/**
	 * 设备类型
	 */
	private String deviceType;
	
	/**
	 * 区域
	 */
	private String area;
	
	/**
	 * 访客类型
	 */
	private String visitor;
}
