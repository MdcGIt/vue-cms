package com.ruoyi.contentcore.domain.dto;

import java.util.List;
import java.util.Map;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.system.validator.LongId;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogUpdateDTO extends BaseDTO {

	/*
	 * 栏目ID
	 */
	@LongId
    private Long catalogId;

    /*
     * 栏目名称 
     */
    @NotBlank
    private String name;

    /*
     * 栏目logo 
     */
    private String logo;

    /*
     * 栏目别名
     */
    @NotBlank
    private String alias;

    /*
     * 栏目描述
     */
    private String description;

    /*
     * 栏目类型
     */
    @NotBlank
    private String catalogType;
    
    /*
     * 栏目目录
     */
    @NotBlank
    private String path;
    
    /*
     * 标题栏目跳转地址
     */
    private String redirectUrl;

    /*
     * SEO关键词
     */
    private String seoKeywords;

    /*
     * SEO描述
     */
    private String seoDescription;

    /*
     * SEO标题
     */
    private String seoTitle;

    /*
     * 栏目发布通道数据
     */
    private List<PublishPipeProp> publishPipeDatas;
    
    /*
     * 自定义参数
     */
    private Map<String, Object> params;
}
