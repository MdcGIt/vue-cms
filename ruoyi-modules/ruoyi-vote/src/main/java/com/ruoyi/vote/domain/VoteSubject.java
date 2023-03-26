package com.ruoyi.vote.domain;

import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 调查投票主题表
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(value = VoteSubject.TABLE_NAME)
public class VoteSubject extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "cc_vote_subject";

	@TableId(value = "subject_id", type = IdType.INPUT)
	private Long subjectId;

	/**
	 * 关联调查投票表ID
	 */
	private Long voteId;
	
	/**
	 * 类型（单选、多选、输入）
	 */
	private String type;
	
	/**
	 * 标题
	 */
	private String title;

	/**
	 * 排序标识
	 */
	private Long sortFlag;
	
	/**
	 * 选项列表
	 */
	@TableField(exist = false)
	private List<VoteSubjectItem> itemList;
}
