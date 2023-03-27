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
	 * 主题投票信息
	 */
	private List<VoteSubjectItem> itemList;
}
