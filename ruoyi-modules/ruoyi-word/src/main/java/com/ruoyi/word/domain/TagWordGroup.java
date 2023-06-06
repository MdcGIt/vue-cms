package com.ruoyi.word.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 标签分组表 [cc_tag_word_group]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(TagWordGroup.TABLE_NAME)
public class TagWordGroup extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cc_tag_word_group";

    @TableId(value = "group_id", type = IdType.INPUT)
    private Long groupId;

    /**
     * 父级ID
     */
    private Long parentId;

    /**
     * 名称
     */
    private String name;

    /**
     * 编码，唯一标识
     */
    private String code;

    /**
     * 排序标识
     */
    private Long sortFlag;
}
