package com.ruoyi.vote.domain.dto;

import java.util.List;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.vote.domain.VoteSubjectItem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveSubjectItemsDTO extends BaseDTO {

	/**
	 * 主题ID
	 */
	private Long subjectId;

	/**
	 * 问卷调查主题选项列表
	 */
	private List<VoteSubjectItem> itemList;
}
