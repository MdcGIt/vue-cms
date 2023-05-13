package com.ruoyi.contentcore.core.impl;

import java.io.IOException;

import com.ruoyi.contentcore.util.CatalogUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.core.InternalURL;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IPublishService;

import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(IInternalDataType.BEAN_NAME_PREFIX + InternalDataType_Catalog.ID)
public class InternalDataType_Catalog implements IInternalDataType {

	public final static String ID = "catalog";
	
	private final ICatalogService catalogService;
	
	private final IPublishService publishService;
	
	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public String getPageData(Long dataId, int pageIndex, String publishPipeCode, boolean isPreview) throws IOException, TemplateException {
		CmsCatalog catalog = catalogService.getCatalog(dataId);
		return this.publishService.getCatalogPageData(catalog, pageIndex, publishPipeCode, isPreview);
	}

	@Override
	public String getLink(InternalURL internalUrl, int pageIndex, String publishPipeCode, boolean isPreview) {
		CmsCatalog catalog = catalogService.getCatalog(internalUrl.getId());
		return this.catalogService.getCatalogLink(catalog, pageIndex, publishPipeCode, isPreview);
	}
}
