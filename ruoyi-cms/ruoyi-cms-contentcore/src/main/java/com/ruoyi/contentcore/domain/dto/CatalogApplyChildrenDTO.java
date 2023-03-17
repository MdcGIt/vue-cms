package com.ruoyi.contentcore.domain.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import com.ruoyi.common.security.domain.BaseDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogApplyChildrenDTO extends BaseDTO {

	/*
	 * 源栏目ID
	 */
	@NotNull(message = "参数不能为空：catalogId")
	public Long catalogId;

	/*
	 * 指定更新的目标栏目Ids，此参数为空时更新源栏目的所有子栏目
	 */
	public List<Long> toCatalogIds;

	/*
	 * 发布通道编码，应用发布通道属性值时必填项
	 */
	public String publishPipeCode;

	/*
	 * 应用发布通道属性Key列表
	 */
	public List<String> publishPipePropKeys;

	/*
	 * 应用发布通道属性值列表，源栏目为空时取此字段值
	 */
	public List<String> publishPipePropValues;

	/*
	 * 是否覆盖所有扩展属性配置
	 */
	public boolean allExtends;
	
	/*
	 * 指定应用的扩展属性Keys
	 */
	public List<String> configPropKeys;
}
