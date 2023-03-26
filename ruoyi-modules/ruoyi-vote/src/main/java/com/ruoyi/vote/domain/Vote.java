package com.ruoyi.vote.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 投票调查表
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(value = Vote.TABLE_NAME)
public class Vote extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "cc_vote";

	@TableId(value = "vote_id", type = IdType.INPUT)
	private Long voteId;

	/**
	 * 唯一标识编码
	 */
	private String code;

	/**
	 * 投票标题
	 */
	private String title;

	/**
	 * 投票开始时间
	 */
	private LocalDateTime startTime;

	/**
	 * 投票结束时间
	 */
	private LocalDateTime endTime;
	
	/**
	 * 投票用户类型（IP、浏览器指纹，会员）
	 */
	private String userType;
	
	/**
	 * 每日投票限制次数
	 */
	private Integer dayLimit;
	
	/**
	 * 总共可投票次数
	 */
	private String totalLimit;
	
	/**
	 * 状态
	 * @see com.ruoyi.vote.fixed.dict.VoteStatus
	 */
	private Integer status;

	/**
	 * 结果查看方式（不允许查看、投票后可看、不限制）
	 */
	private String resultViewType;
	
	/**
	 * 主题列表
	 */
	@TableField(exist = false)
	private List<VoteSubject> subjectList;
}
