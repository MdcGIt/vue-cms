package com.ruoyi.advertisement.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 广告日点击/展现统计数据
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(CmsAdDayStat.TABLE_NAME)
public class CmsAdDayStat implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cms_ad_daystat";

	@TableId(value = "stat_id", type = IdType.AUTO)
	private Long statId;
	
	/**
	 * 统计日期，格式：yyyyMMdd
	 */
	private String day;
	
	/**
	 * 广告ID
	 */
	private Long advertisementId;
	
	/**
	 * 点击数
	 */
	private Integer click;
	
	/**
	 * 展现数
	 */
	private Integer view;
	
	/**
	 * 广告名称
	 */
	@TableField(exist = false)
	private String adName;
}
