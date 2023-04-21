package com.ruoyi.contentcore.domain.dto;

import java.util.List;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SiteDefaultTemplateDTO extends BaseDTO {

	/*
	 * 站点ID
	 */
	@LongId
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
