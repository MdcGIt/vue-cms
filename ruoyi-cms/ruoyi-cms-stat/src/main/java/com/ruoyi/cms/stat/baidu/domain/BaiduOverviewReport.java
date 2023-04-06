package com.ruoyi.cms.stat.baidu.domain;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 百度概况统计通用数据结构
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
public class BaiduOverviewReport {
	
	/**
	 * 查询指标
	 */
	private List<String> fields;
	
	/**
	 * x轴
	 */
	private List<String> xAxisDatas;
	
	/**
	 * y轴数据
	 */
	private Map<String, List<Object>> datas;
}
