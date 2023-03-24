package com.ruoyi.article.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Getter;
import lombok.Setter;

/**
 * 文章详情表对象 [cms_article_detail]
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(CmsArticleDetail.TABLE_NAME)
public class CmsArticleDetail {

    public static final String TABLE_NAME = "cms_article_detail";

    /**
     * 内容ID
     */
    @TableId(value = "content_id", type = IdType.INPUT)
    private Long contentId;

    /**
     * 正文详情（json）
     */
    private String contentJson;

    /**
     * 正文详情（html）
     */
    private String contentHtml;

    /**
     * 分页标题
     */
    private String pageTitles;

    /**
     * 是否下载远程图片
     */
    private String downloadRemoteImage;
}