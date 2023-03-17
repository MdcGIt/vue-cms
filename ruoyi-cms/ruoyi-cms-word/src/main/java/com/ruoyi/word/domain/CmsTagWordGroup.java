package com.ruoyi.word.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 标签分组表 [cms_tag_word_group]
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(CmsTagWordGroup.TABLE_NAME)
public class CmsTagWordGroup extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_tag_word_group";

    @TableId(value = "group_id", type = IdType.INPUT)
    private Long groupId;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码，唯一标识
     */
    private String code;

    /**
     * 图片路径
     */
    private String logo;

    /**
     * 图片预览路径
     */
    @TableField(exist = false)
    private String src;
    
    /**
     * 排序标识
     */
    private Long sortFlag;
}
