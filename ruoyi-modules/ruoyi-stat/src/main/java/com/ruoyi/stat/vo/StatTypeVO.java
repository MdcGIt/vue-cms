package com.ruoyi.stat.vo;

import com.ruoyi.common.i18n.I18nField;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatTypeVO {

	/**
	 * 统计类型
	 */
	private String type;
	
	/**
	 * 分组
	 */
	@I18nField("STAT.GROUP.{type}")
	private String group;
	
	/**
	 * 统计名称
	 */
	@I18nField("STAT.NAME.{type}")
	private String name;
}
