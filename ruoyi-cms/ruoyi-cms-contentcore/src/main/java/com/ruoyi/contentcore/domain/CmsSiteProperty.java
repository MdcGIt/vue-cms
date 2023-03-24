package com.ruoyi.contentcore.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 站点自定义属性表对象 [cms_site_property]
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(CmsSiteProperty.TABLE_NAME)
public class CmsSiteProperty extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_site_property";

    /**
     * 属性ID-主键
     */
    @TableId(value = "property_id", type = IdType.INPUT)
    private Long propertyId;

    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 属性名称
     */
    private String propName;

    /**
     * 属性代码
     */
    private String propCode;

    /**
     * 属性值
     */
    private String propValue;
}