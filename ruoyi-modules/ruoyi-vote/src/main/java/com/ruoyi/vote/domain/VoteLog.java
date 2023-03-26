package com.ruoyi.vote.domain;

import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 调查投票日志表
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(value = VoteLog.TABLE_NAME, autoResultMap = true)
public class VoteLog extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "cc_vote_log";

	@TableId(value = "log_id", type = IdType.INPUT)
	private Long logId;

	/**
	 * 关联调查投票表ID
	 */
	private Long voteId;
	
	/**
	 * 投票人类型
	 */
	private String userType;
	
	/**
	 * 投票人ID
	 */
	private String userId;

	/**
	 * 投票结果
	 * 
	 * 格式：<subjectId, itemId|inputText>
	 */
	@TableField(typeHandler = JacksonTypeHandler.class)
	private Map<String, String> result;
	
	/**
	 * IP
	 */
	private String ip;
	
	/**
	 * UserAgent
	 */
	private String userAgent;
}
