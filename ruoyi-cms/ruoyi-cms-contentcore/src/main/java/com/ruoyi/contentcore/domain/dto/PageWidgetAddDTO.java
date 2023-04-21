package com.ruoyi.contentcore.domain.dto;

import com.ruoyi.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageWidgetAddDTO {

	/**
	 * 栏目ID
	 */
	@LongId
	private Long catalogId;

	/**
	 * 页面部件类型
	 */
	@NotEmpty
	private String type;

	/**
	 * 名称
	 */
	@NotEmpty
	private String name;

	/**
	 * 编码
	 */
	@NotEmpty
	private String code;

	/**
	 * 发布通道编码
	 */
	@NotEmpty
    private String publishPipeCode;

	/**
	 * 模板
	 */
	@NotEmpty
	private String template;

	/**
	 * 静态化目录
	 */
	@NotEmpty
	private String path;
    
	/**
	 * 备注
	 */
    private String remark;
}
