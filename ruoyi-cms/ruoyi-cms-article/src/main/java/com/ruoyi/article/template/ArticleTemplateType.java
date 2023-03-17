package com.ruoyi.article.template;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ruoyi.article.domain.CmsArticleDetail;
import com.ruoyi.article.mapper.CmsArticleDetailMapper;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.ConvertUtils;
import com.ruoyi.common.utils.ReflectASMUtils;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.enums.ContentCopyType;
import com.ruoyi.contentcore.mapper.CmsContentMapper;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.template.ITemplateType;
import com.ruoyi.contentcore.util.TemplateUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(ITemplateType.BEAN_NAME_PREFIX + ArticleTemplateType.TypeId)
public class ArticleTemplateType implements ITemplateType {
	
	public final static String TypeId = "article";
	
	private final CmsContentMapper contentMapper;
	
	private final CmsArticleDetailMapper articleMapper;

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	@Override
	public String getId() {
		return TypeId;
	}

	@Override
	public void initTemplateData(Object dataId, TemplateContext context) {
		CmsContent content = this.contentMapper.selectById(ConvertUtils.toLong(dataId));
		CmsArticleDetail articleDetail = null;
		if (ContentCopyType.isMapping(content.getCopyType())) {
			articleDetail = this.articleMapper.selectById(content.getCopyId());
		} else {
			articleDetail = this.articleMapper.selectById(content.getContentId());
		}
		Map<String, Object> articleMap = new HashMap<>();
		articleMap.putAll(ReflectASMUtils.beanToMap(content));
		articleMap.putAll(ReflectASMUtils.beanToMap(articleDetail));
		context.getVariables().put(TemplateUtils.TemplateVariable_Content, articleMap);

		CmsSite site = this.siteService.getSite(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		TemplateUtils.addCatalogVariables(site, catalog, context);
	}
}
