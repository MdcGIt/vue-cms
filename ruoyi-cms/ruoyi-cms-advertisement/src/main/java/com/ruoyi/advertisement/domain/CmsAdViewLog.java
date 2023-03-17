package com.ruoyi.advertisement.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.stat.events.RequestEvent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(CmsAdViewLog.TABLE_NAME)
public class CmsAdViewLog extends RequestEvent {
	
	public final static String TABLE_NAME = "cms_ad_view_log";

	@TableId(value = "log_id", type = IdType.AUTO)
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
	private String adName;
	
	/**
	 * 广告版位编码
	 */
	private Long adSpaceCode;
}
