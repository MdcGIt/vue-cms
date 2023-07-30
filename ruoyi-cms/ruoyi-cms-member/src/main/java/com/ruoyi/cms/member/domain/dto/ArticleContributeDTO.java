package com.ruoyi.cms.member.domain.dto;

import com.ruoyi.system.validator.LongId;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.util.List;

/**
 * 文章投稿 DTO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class ArticleContributeDTO {

    /**
     * 分类ID
     */
    @LongId
    private Long catalogId;

    /**
     * 标题
     */
    @NotBlank
    @Length(max = 120)
    private String title;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 文章标签
     */
    private List<String> tags;

    /**
     * 正文内容
     */
    @NotBlank
    @Length(max = 10000)
    private String contentHtml;

    /**
     * 引导图
     */
    private String logo;
}
