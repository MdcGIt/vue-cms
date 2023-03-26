package com.ruoyi.vote.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 调查投票主题选项表
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(value = VoteSubjectItem.TABLE_NAME)
public class VoteSubjectItem extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "cc_vote_item";

	@TableId(value = "item_id", type = IdType.INPUT)
	private Long itemId;

	/**
	 * 关联调查投票表ID
	 */
	private Long voteId;

	/**
	 * 关联主题ID
	 */
	private Long subjectId;
	
	/**
	 * 类型（文字、图片、内容引用）
	 */
	private String type;
	
	/**
	 * 选项内容（文字内容、图片地址、内容引用地址）
	 */
	private String content;
	
	/**
	 * 选项描述
	 */
	private String desc;

	/**
	 * 排序标识
	 */
	private Long sortFlag;
	
	/**
	 * 投票数
	 */
	private Integer total;
}
