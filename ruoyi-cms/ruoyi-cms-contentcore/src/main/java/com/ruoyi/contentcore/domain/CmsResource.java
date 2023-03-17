package com.ruoyi.contentcore.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;
import com.ruoyi.system.fixed.dict.EnableOrDisable;

import lombok.Getter;
import lombok.Setter;

/**
 * 资源表对象 [cms_resource]
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(CmsResource.TABLE_NAME)
public class CmsResource extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_resource";

    /**
     * 资源ID
     */
    @TableId(value = "resource_id", type = IdType.ASSIGN_ID)
    private Long resourceId;

    /**
     * 站点id
     */
    private Long siteId;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 存储类型，默认：local
     */
    private String storageType;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 资源路径（相对站点根目录路径）
     */
    private String path;

    /**
     * 文件名称
     */
    private String fileName;
    
    /**
     * 后缀名，不带.
     */
    private String suffix;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 来源地址
     */
    private String sourceUrl;

    /**
     * 状态
     */
    private String status;

    /**
     * 引用关系
     */
    private String usageInfo;
    
    @TableField(exist = false)
    private String src;
    
    @TableField(exist = false)
    private String internalUrl;
    
    public boolean isEnable() {
    	return EnableOrDisable.isEnable(this.status);
    }
}
