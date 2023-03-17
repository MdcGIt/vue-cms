package com.ruoyi.contentcore.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageWidgetEditDTO {

	/**
	 * 页面部件ID
	 */
	@NotEmpty
	@Min(1)
    private Long pageWidgetId;

	/**
	 * 名称
	 */
    @NotBlank
    private String name;

    /**
     * 发布通道编码
     */
    @NotEmpty
    private String publishPipeCode;
    
    /**
     * 模板路径
     */
    @NotEmpty
    private String template;
    
    /**
     * 静态化目录
     */
    @NotEmpty
    private String path;
    
    /**
     * 页面部件内容
     */
    private String contentStr;
    
    /**
     * 备注
     */
    private String remark;
}
