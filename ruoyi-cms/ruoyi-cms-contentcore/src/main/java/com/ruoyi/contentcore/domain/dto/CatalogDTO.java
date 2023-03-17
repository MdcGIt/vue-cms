package com.ruoyi.contentcore.domain.dto;

import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.contentcore.domain.CmsCatalog;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatalogDTO extends BaseDTO {

	/*
	 * 栏目ID
	 */
    private Long catalogId;

    /*
     * 站点ID
     */
    private Long siteId;

    /*
     * 父级栏目ID
     */
    private Long parentId;

    /*
     * 父级栏目名称
     */
    private String parentName;

    /*
     * 栏目名称 
     */
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
    private String catalogType;
    
    /*
     * 栏目目录
     */
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
    private Map<String, Object> configProps;

    /*
     * 栏目发布通道数据
     */
    private List<PublishPipeProp> publishPipeDatas;
    
    /*
     * 自定义参数
     */
    private Map<String, Object> params;
    
    public static CatalogDTO newInstance(CmsCatalog catalog) {
    	CatalogDTO dto = new CatalogDTO();
    	BeanUtils.copyProperties(catalog, dto);
    	return dto;
    }
}
