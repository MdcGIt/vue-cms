package com.ruoyi.cms.member.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName(CmsMemberLike.TABLE_NAME)
public class CmsMemberLike implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public final static String TABLE_NAME = "cms_member_like";

	@TableId(value = "log_id", type = IdType.AUTO)
	private Long logId;

	/**
	 * 站点ID
	 */
	private Long siteId;
	
	/**
	 * 内容ID
	 */
	private Long contentId;

	/**
	 * 会员ID
	 */
	private Long memberId;

	/**
	 * 点赞时间
	 */
	private LocalDateTime createTime;
}
