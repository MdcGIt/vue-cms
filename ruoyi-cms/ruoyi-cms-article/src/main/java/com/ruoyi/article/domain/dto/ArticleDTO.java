package com.ruoyi.article.domain.dto;

import org.springframework.beans.BeanUtils;

import com.ruoyi.article.domain.CmsArticleDetail;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.dto.ContentDTO;
import com.ruoyi.contentcore.fixed.dict.ContentAttribute;
import com.ruoyi.contentcore.util.InternalUrlUtils;

import lombok.Getter;
import lombok.Setter;

@Getter 
@Setter
public class ArticleDTO extends ContentDTO {

	/**
	 * 文章正文（html格式）
	 */
    private String contentHtml;

    /**
     * 是否下载远程图片
     */
    private String downloadRemoteImage;

    /**
     * 分页标题
     */
    private String pageTitles;

	public static ArticleDTO newInstance(CmsContent content, CmsArticleDetail articleDetail) {
		ArticleDTO dto = new ArticleDTO();
		BeanUtils.copyProperties(content, dto);
		dto.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
		BeanUtils.copyProperties(articleDetail, dto);
		return dto;
	}
}
