package com.ruoyi.contentcore.domain.dto;

import java.util.List;

import com.ruoyi.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CopyContentDTO extends BaseDTO {

	/**
	 * 复制类型<br/>
	 * 
	 * @see com.ruoyi.contentcore.ContentCoreConsts
	 */
	@NotNull
	public Integer copyType;
	
	/**
	 * 复制内容IDs
	 */
	@NotEmpty
	public List<Long> contentIds;
	
	/**
	 * 目标栏目IDs
	 */
	@NotEmpty
	public List<Long> catalogIds;
}
