package com.ruoyi.advertisement.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.stat.RequestEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(CmsAdClickLog.TABLE_NAME)
public class CmsAdClickLog extends RequestEvent implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cms_ad_click_log";

	@TableId(value = "log_id", type = IdType.INPUT)
	private Long logId;
	
	/**
	 * 站点ID
	 */
	private Long siteId;
	
	/**
	 * 广告ID
	 */
	private Long adId;
	
	/**
	 * 广告名称
	 */
	@TableField(exist = false)
	private String adName;
}
