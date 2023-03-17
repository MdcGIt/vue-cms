package com.ruoyi.word.listener;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ruoyi.article.ArticleContentType;
import com.ruoyi.article.domain.CmsArticleDetail;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.listener.event.BeforeContentSaveEvent;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.word.properties.HotWordGroupsProperty;
import com.ruoyi.word.service.IErrorProneWordService;
import com.ruoyi.word.service.IHotWordService;
import com.ruoyi.word.service.ISensitiveWordService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WordEventListener {
	
	private final ISensitiveWordService sensitiveWordService;
	
	private final IHotWordService hotWordService;
	
	private final IErrorProneWordService errorProneWordService;
	
	private final ISiteService siteService;

	@EventListener
	public void afterContentSave(BeforeContentSaveEvent event) {
		IContent<?> content = event.getContent();
		if (ArticleContentType.ID.equals(content.getContentType())) {
			CmsArticleDetail articleDetail = (CmsArticleDetail) content.getExtendEntity();
			String contentHtml = articleDetail.getContentHtml();
			// 敏感词处理
			contentHtml = sensitiveWordService.replaceSensitiveWords(contentHtml, null);
			// 易错词处理
			contentHtml = errorProneWordService.replaceErrorProneWords(contentHtml);
			// 热词处理
			CmsSite site = siteService.getSite(content.getCatalog().getSiteId());
			Long[] groupIds = HotWordGroupsProperty.getHotWordGroupIds(content.getCatalog().getConfigProps(), site.getConfigProps());
			if (Objects.nonNull(groupIds) && groupIds.length > 0) {
				contentHtml = hotWordService.replaceHotWords(contentHtml, groupIds, null, null);
			}
			articleDetail.setContentHtml(contentHtml);
		}
	}
}

