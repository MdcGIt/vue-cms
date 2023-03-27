package com.ruoyi.vote.domain;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;
import com.ruoyi.system.fixed.dict.UserStatus;
import com.ruoyi.system.validator.Dict;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
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
	@Pattern(regexp = "^[A-Za-z0-9_]+$", message = "编码不能为空且只能使用大小写字母及数字组合")
	private String code;

	/**
	 * 投票标题
	 */
	@NotEmpty
	private String title;

	/**
	 * 投票开始时间
	 */
	@NotEmpty
	private LocalDateTime startTime;

	/**
	 * 投票结束时间
	 */
	@NotEmpty
	private LocalDateTime endTime;

	/**
	 * 投票用户类型（IP、浏览器指纹，登录用户）
	 */
	@NotEmpty
	private String userType;

	/**
	 * 每日投票限制次数
	 */
	@Min(1)
	private Integer dayLimit;

	/**
	 * 总共可投票次数
	 */
	@Min(1)
	private String totalLimit;

	/**
	 * 状态
	 * 
	 * @see com.ruoyi.vote.fixed.dict.VoteStatus
	 */
	@Dict(UserStatus.TYPE)
	private String status;

	/**
	 * 结果查看方式（不允许查看、投票后可看、不限制）
	 */
	@NotEmpty
	private String viewType;

	/**
	 * 投票总人数
	 */
	private Integer total;
	
	/**
	 * 来源归属标识，例如：cms:siteid
	 */
	private String source;

	/**
	 * 主题列表
	 */
	@TableField(exist = false)
	private List<VoteSubject> subjectList;
}
