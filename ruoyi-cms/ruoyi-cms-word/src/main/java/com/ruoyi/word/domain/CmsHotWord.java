package com.ruoyi.word.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 热词表对象[cms_hot_word]
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(CmsHotWord.TABLE_NAME)
public class CmsHotWord extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_hot_word";

    @TableId(value = "word_id", type = IdType.INPUT)
    private Long wordId;

    /**
     * 所属分组ID
     */
    private Long groupId;

    /**
     * 所属站点ID
     */
    private Long siteId;

    /**
     * 词汇
     */
    private String word;

    /**
     * 链接
     */
    private String url;
    
    /**
     * 链接打开方式
     */
    private String urlTarget;
    
    /**
     * 引用次数
     */
    private long useCount;
    
    /**
     * 点击次数
     */
    private long hitCount;
}
