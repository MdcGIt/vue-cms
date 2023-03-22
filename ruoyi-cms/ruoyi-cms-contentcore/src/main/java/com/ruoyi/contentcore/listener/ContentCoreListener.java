package com.ruoyi.contentcore.listener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.contentcore.core.impl.InternalDataType_Content;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsPublishPipe;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.enums.ContentCopyType;
import com.ruoyi.contentcore.fixed.dict.ContentStatus;
import com.ruoyi.contentcore.listener.event.AfterCatalogSaveEvent;
import com.ruoyi.contentcore.listener.event.AfterContentOfflineEvent;
import com.ruoyi.contentcore.listener.event.AfterSiteDeleteEvent;
import com.ruoyi.contentcore.listener.event.BeforeCatalogDeleteEvent;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.IPageWidgetService;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ContentCoreListener {

	private final ISiteService siteService;

	private final IPublishPipeService publishPipeService;

	private final IContentService contentService;

	private final IPageWidgetService pageWidgetService;
	
	private final AsyncTaskManager asyncTaskManager;

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
	public void beforeCatalogDelete(BeforeCatalogDeleteEvent event) {
		CmsCatalog catalog = event.getCatalog();
		// 删除栏目内容
		this.contentService.deleteContentsByCatalog(catalog);
		// 删除页面部件
		this.pageWidgetService.deletePageWidgetsByCatalog(catalog);
	}

	@EventListener
	public void afterContentOffline(AfterContentOfflineEvent event) {
		final Long contentId = event.getContent().getContentEntity().getContentId();
		final String operator = event.getContent().getOperator().getUsername();
		AsyncTask task = new AsyncTask() {

			@Override
			public void run0() throws Exception {
				// 映射关联内容同步下线
				List<CmsContent> mappingList = contentService.lambdaQuery()
						.gt(CmsContent::getCopyType, ContentCopyType.Mapping.value())
						.eq(CmsContent::getCopyId, contentId).list();
				for (CmsContent c : mappingList) {
					if (ContentStatus.PUBLISHED == c.getStatus()) {
						try {
							contentService.deleteStaticFiles(c);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					c.setStatus(ContentStatus.OFFLINE);
					c.updateBy(operator);
				}
				contentService.updateBatchById(mappingList);
				// 标题内容同步下线
				String internalUrl = InternalUrlUtils.getInternalUrl(InternalDataType_Content.ID, contentId);
				List<CmsContent> linkList = contentService.lambdaQuery().eq(CmsContent::getLinkFlag, YesOrNo.YES)
						.eq(CmsContent::getRedirectUrl, internalUrl).list();
				for (CmsContent c : linkList) {
					c.setStatus(ContentStatus.OFFLINE);
					c.updateBy(operator);
				}
				mappingList.addAll(linkList);
				contentService.updateBatchById(mappingList);
			}
		};
		this.asyncTaskManager.execute(task);
	}
}
