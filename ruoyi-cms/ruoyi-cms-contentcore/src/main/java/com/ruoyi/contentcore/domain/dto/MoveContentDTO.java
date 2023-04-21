package com.ruoyi.contentcore.domain.dto;

import java.util.List;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.system.validator.LongId;

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
	public List<Long> contentIds;
	
	/**
	 * 目标栏目ID
	 */
	@LongId
	public Long catalogId;
}
