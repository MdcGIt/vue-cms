package com.ruoyi.contentcore.core.impl;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.core.InternalURL;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.IPublishService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.SiteUtils;

import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(IInternalDataType.BEAN_NAME_PREFIX + InternalDataType_Site.ID)
public class InternalDataType_Site implements IInternalDataType {

	public final static String ID = "site";
	
	private final ISiteService siteService;
	
	private final IPublishService publishService;
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getPageData(Long dataId, int pageIndex, String publishPipeCode, boolean isPreview) throws IOException, TemplateException {
		CmsSite site = siteService.getSite(dataId);
		return this.publishService.getSitePageData(site, publishPipeCode, isPreview);
	}

	@Override
	public String getLink(InternalURL internalUrl, int pageIndex, String publishPipeCode, boolean isPreview) {
		CmsSite site = siteService.getSite(internalUrl.getId());
		return SiteUtils.getSiteLink(site, publishPipeCode, isPreview);
	}
}
