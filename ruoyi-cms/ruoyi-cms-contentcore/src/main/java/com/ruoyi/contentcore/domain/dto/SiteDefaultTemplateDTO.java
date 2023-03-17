package com.ruoyi.contentcore.domain.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

import com.ruoyi.common.security.domain.BaseDTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SiteDefaultTemplateDTO extends BaseDTO {

	/*
	 * 站点ID
	 */
	@NotEmpty
	public Long siteId;

	/*
	 * 已用到指定栏目IDs
	 */
	@NotEmpty
	public List<Long> toCatalogIds;

	/*
	 * 默认模板属性
	 */
	@NotEmpty
	public List<PublishPipeProp> publishPipeProps;
}
