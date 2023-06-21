package com.ruoyi.contentcore.domain.vo;

import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.dto.PublishPipeProp;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogVO {

	/*
	 * 栏目ID
	 */
    private Long catalogId;

    /*
     * 站点ID
     */
	@Min(1)
    private Long siteId;

    /*
     * 父级栏目ID
     */
	@Min(0)
    private Long parentId;

    /*
     * 父级栏目名称
     */
    private String parentName;

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
     * logo预览地址
     */
    private String logoSrc;

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
     * 所属部门编码
     */
    private String deptCode;

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
     * 是否可见
     */
    private String visibleFlag;

    /*
     * 是否静态化
     */
    private String staticFlag;

    /*
     * 自定义首页文件名
     */
    private String indexFileName;

    /*
     * 列表页文件名规则
     */
    private String listNameRule;

    /*
     * 详情页文件名规则
     */
    private String detailNameRule;

    /*
     * 栏目层级
     */
    private Integer treeLevel;

    /*
     * 子栏目数量
     */
    private Integer childCount;

    /*
     * 当前栏目数量
     */
    private Integer contentCount;

    /*
     * 栏目状态
     */
    private Integer status;

    /*
     * 点击量
     */
    private Integer hitCount;

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
     * 栏目链接
     */
    private String link;
    
    /*
     * 栏目扩展配置
     */
    private Map<String, String> configProps;

    /*
     * 栏目发布通道数据
     */
    private List<PublishPipeProp> publishPipeDatas;
    
    public static CatalogVO newInstance(CmsCatalog catalog) {
    	CatalogVO dto = new CatalogVO();
    	BeanUtils.copyProperties(catalog, dto);
    	return dto;
    }
}
