package com.ruoyi.contentcore.listener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.ruoyi.contentcore.util.SiteUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsPublishPipe;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.listener.event.AfterCatalogDeleteEvent;
import com.ruoyi.contentcore.listener.event.AfterCatalogSaveEvent;
import com.ruoyi.contentcore.listener.event.AfterSiteDeleteEvent;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.ISiteService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContentCoreListener {

	private final ISiteService siteService;

	private final IPublishPipeService publishPipeService;

	private final IContentService contentService;

	@EventListener
	public void afterSiteDelete(AfterSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		this.publishPipeService.deletePublishPipeBySite(site);
	}

	@EventListener
	public void afterCatalogSave(AfterCatalogSaveEvent event) {
		CmsCatalog catalog = event.getCatalog();
		// 栏目路径变更，各发布通道路径变更
		if (!event.getOldPath().equals(catalog.getPath())) {
			List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(catalog.getSiteId());
			if (publishPipes.size() > 0) {
				CmsSite site = this.siteService.getSite(catalog.getSiteId());
				for (CmsPublishPipe publishPipe : publishPipes) {
					String siteRoot = SiteUtils.getSiteRoot(site, publishPipe.getCode());
					try {
						Files.move(Paths.get(siteRoot + event.getOldPath()), Paths.get(siteRoot + catalog.getPath()),
								StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@EventListener
	public void afterCatalogDelete(AfterCatalogDeleteEvent event) {
		CmsCatalog catalog = event.getCatalog();
		this.contentService.deleteContentsByCatalogId(catalog.getCatalogId());
	}
}
