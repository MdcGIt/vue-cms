package com.ruoyi.contentcore.domain.dto;

import com.ruoyi.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoveContentDTO extends BaseDTO {

	/**
	 * 转移内容IDs
	 */
	@NotEmpty
	public Long[] contentIds;
	
	/**
	 * 目标栏目ID
	 */
	@NotEmpty
	public Long catalogId;
}
