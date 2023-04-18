package com.ruoyi.contentcore.domain.dto;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.system.validator.LongId;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortContentDTO extends BaseDTO {

	/**
	 * 排序内容ID
	 */
	@LongId
	public Long contentId;
	
	/**
	 * 排序目标内容ID
	 */
	@LongId
	public Long targetContentId;
}
