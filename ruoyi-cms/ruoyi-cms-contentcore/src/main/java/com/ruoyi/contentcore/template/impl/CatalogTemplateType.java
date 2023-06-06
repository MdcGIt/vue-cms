package com.ruoyi.contentcore.template.impl;

import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.ConvertUtils;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.properties.CatalogPageSizeProperty;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.ISiteService;
import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.template.ITemplateType;
import com.ruoyi.contentcore.util.TemplateUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(ITemplateType.BEAN_NAME_PREFIX + CatalogTemplateType.TypeId)
public class CatalogTemplateType implements ITemplateType {
	
	public final static String TypeId = "CatalogIndex";

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	@Override
	public String getId() {
		return TypeId;
	}

	@Override
	public void initTemplateData(Object dataId, TemplateContext context) {
		CmsCatalog catalog = this.catalogService.getCatalog(ConvertUtils.toLong(dataId));
		CmsSite site = this.siteService.getSite(catalog.getSiteId());
		TemplateUtils.addCatalogVariables(site, catalog, context);

		int pageSize = CatalogPageSizeProperty.getValue(catalog.getConfigProps(), site.getConfigProps());
		context.setPageSize(pageSize);
	}
}
