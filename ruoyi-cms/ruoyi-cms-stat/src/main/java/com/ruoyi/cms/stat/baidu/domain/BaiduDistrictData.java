package com.ruoyi.cms.stat.baidu.domain;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaiduDistrictData {

	/**
	 * 地区
	 */
	private String dictrict;
	
	/**
	 * 访问量
	 */
	private Integer pv;
	
	/**
	 * 占比
	 */
	private Double ratio;
}
