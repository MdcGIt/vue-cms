package com.ruoyi.contentcore.domain.dto;

import java.util.List;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogApplyPublishPipeDTO extends BaseDTO {

	/*
	 * 源栏目ID
	 */
	@LongId
	public Long catalogId;

	/*
	 * 发布通道编码，应用发布通道属性值时必填项
	 */
	public String publishPipeCode;

	/*
	 * 应用发布通道属性Key列表
	 */
	@NotNull
	@NotEmpty
	public List<String> publishPipePropKeys;
}
