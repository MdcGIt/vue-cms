package com.ruoyi.contentcore.template.impl;

import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.ConvertUtils;
import com.ruoyi.common.utils.ReflectASMUtils;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.fixed.dict.ContentAttribute;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.template.ITemplateType;
import com.ruoyi.contentcore.util.TemplateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component(ITemplateType.BEAN_NAME_PREFIX + ContentTemplateType.TypeId)
public class ContentTemplateType implements ITemplateType {

	public final static String TypeId = "Content";

	private final IContentService contentService;

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	@Override
	public String getId() {
		return TypeId;
	}

	@Override
	public void initTemplateData(Object dataId, TemplateContext context) {
		CmsContent content = this.contentService.getById(ConvertUtils.toLong(dataId));
		Map<String, Object> contentMap = ReflectASMUtils.beanToMap(content);
		String link = this.contentService.getContentLink(content, 1,
				context.getPublishPipeCode(), context.isPreview());
		contentMap.put(TemplateUtils.TemplateVariable_OBJ_Link, link);
		contentMap.put("attributes", ContentAttribute.convertStr(content.getAttributes()));
		context.getVariables().put(TemplateUtils.TemplateVariable_Content, contentMap);

		CmsSite site = this.siteService.getSite(content.getSiteId());
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		TemplateUtils.addCatalogVariables(site, catalog, context);
	}
}
