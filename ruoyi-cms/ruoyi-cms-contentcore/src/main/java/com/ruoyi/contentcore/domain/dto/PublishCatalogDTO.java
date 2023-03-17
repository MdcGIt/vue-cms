package com.ruoyi.contentcore.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublishCatalogDTO {

	/**
	 * 栏目ID
	 */
	@NotNull
	private Long catalogId;
	
	/**
	 * 是否发布子栏目
	 */
	@NotNull
	private boolean publishChild;
	
	/**
	 * 是否发布栏目内容
	 */
	@NotNull
	private boolean publishDetail;
	
	/**
	 * 发布内容状态
	 */
	@NotEmpty
	private String publishStatus;
}
