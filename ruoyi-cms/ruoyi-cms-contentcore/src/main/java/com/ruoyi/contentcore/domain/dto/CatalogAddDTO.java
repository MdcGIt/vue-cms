package com.ruoyi.contentcore.domain.dto;

import com.ruoyi.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogAddDTO extends BaseDTO {
	
	/**
	 * 所属站点ID
	 */
	private Long siteId;

    /**
     * 父级栏目ID
     */
    private Long parentId = 0L;

    /**
     * 栏目名称 
     */
    @NotBlank
    private String name;
    
    /**
     * 栏目别名
     */
    @NotBlank
    private String alias;
    
    /**
     * 栏目目录
     */
    @NotBlank
    private String path;

    /**
     * 栏目类型
     */
    @NotBlank
    private String catalogType;
}
