package com.ruoyi.contentcore.domain.dto;

import com.ruoyi.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortContentDTO extends BaseDTO {

	/**
	 * 排序内容ID
	 */
	@NotEmpty
	public Long contentId;
	
	/**
	 * 排序目标内容ID
	 */
	@NotEmpty
	public Long targetContentId;
}
