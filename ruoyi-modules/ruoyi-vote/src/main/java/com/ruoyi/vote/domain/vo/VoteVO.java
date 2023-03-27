package com.ruoyi.vote.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteVO {

	/**
	 * ID
	 */
	private Long voteId;
	
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
	 * 每日投票限制次数
	 */
	private Integer dayLimit;
	
	/**
	 * 总共可投票次数
	 */
	private String totalLimit;
	
	/**
	 * 结果查看方式（不允许查看、投票后可看、不限制）
	 */
	private String viewType;
	
	/**
	 * 已投票人数
	 */
	private Integer total;
	
	/**
	 * 主题列表
	 */
	private List<VoteSubjectVO> subjects;
}
