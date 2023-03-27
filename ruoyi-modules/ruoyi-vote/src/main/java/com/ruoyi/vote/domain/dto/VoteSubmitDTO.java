package com.ruoyi.vote.domain.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoteSubmitDTO {

	/**
	 * ID
	 */
	private Long voteId;

	/**
	 * 主题投票信息
	 */
	private List<Subject> subjects;

	@Getter
	@Setter
	static class Subject {

		private Long subjectId;

		/**
		 * 选项信息
		 */
		private Long itemId;

		/**
		 * 输入信息
		 */
		private String input;
	}
}
