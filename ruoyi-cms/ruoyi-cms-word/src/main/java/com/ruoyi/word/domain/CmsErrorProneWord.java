package com.ruoyi.word.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * 易错词表 [CmsErrorProneWord]
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(CmsErrorProneWord.TABLE_NAME)
public class CmsErrorProneWord extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_error_prone_word";

    @TableId(value = "word_id", type = IdType.INPUT)
    private Long wordId;

    /**
     * 名称
     */
    private String word;

    /**
     * 替换词
     */
    private String replaceWord;
}
