package com.ruoyi.advertisement.listener;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.advertisement.AdSpacePageWidgetType;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.listener.event.SiteExportEvent;
import com.ruoyi.contentcore.service.IPageWidgetService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.advertisement.domain.CmsAdvertisement;
import com.ruoyi.advertisement.service.IAdvertisementService;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.listener.event.BeforeSiteDeleteEvent;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AdvertisementListener {

	private final IPageWidgetService pageWidgetService;

	private final IAdvertisementService advertisementService;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		// 删除广告数据
		try {
			long total = this.advertisementService
					.count(new LambdaQueryWrapper<CmsAdvertisement>().eq(CmsAdvertisement::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除广告数据：" + (i * pageSize) + "/" + total);
				this.advertisementService.remove(new LambdaQueryWrapper<CmsAdvertisement>()
						.eq(CmsAdvertisement::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除广告数据错误：" + e.getMessage());
		}
	}
	@EventListener
	public void onSiteExport(SiteExportEvent event) throws IOException {
		// cms_page_widget
		{
			long total = pageWidgetService.count(new LambdaQueryWrapper<CmsPageWidget>()
					.eq(CmsPageWidget::getSiteId, event.getSite().getSiteId())
					.eq(CmsPageWidget::getType, AdSpacePageWidgetType.ID));
			int pageNumber = 1;
			long offset = 0;
			long limit = 500;
			while (total > 0) {
				LambdaQueryWrapper<CmsPageWidget> q = new LambdaQueryWrapper<CmsPageWidget>()
						.eq(CmsPageWidget::getSiteId, event.getSite().getSiteId())
						.eq(CmsPageWidget::getType, AdSpacePageWidgetType.ID)
						.gt(CmsPageWidget::getPageWidgetId, offset)
						.orderByAsc(CmsPageWidget::getPageWidgetId);
				Page<CmsPageWidget> page = pageWidgetService.page(new Page<>(1, limit, false), q);
				String json = JacksonUtils.to(page.getRecords());
				event.getZipBuilder().add(json.getBytes(StandardCharsets.UTF_8))
						.path("db/" + CmsPageWidget.TABLE_NAME + "-" + AdSpacePageWidgetType.ID + "/page" + pageNumber + ".json")
						.save();
				offset = page.getRecords().get(page.getRecords().size() - 1).getPageWidgetId();
				total -= page.getRecords().size();
			}
		}
		// cms_advertisement
		{
			long total = advertisementService.count(new LambdaQueryWrapper<CmsAdvertisement>()
					.eq(CmsAdvertisement::getSiteId, event.getSite().getSiteId()));
			int pageNumber = 1;
			long offset = 0;
			long limit = 1000;
			while (total > 0) {
				LambdaQueryWrapper<CmsAdvertisement> q = new LambdaQueryWrapper<CmsAdvertisement>()
						.eq(CmsAdvertisement::getSiteId, event.getSite().getSiteId())
						.gt(CmsAdvertisement::getAdvertisementId, offset)
						.orderByAsc(CmsAdvertisement::getAdvertisementId);
				Page<CmsAdvertisement> page = advertisementService.page(new Page<>(1, limit, false), q);
				String json = JacksonUtils.to(page.getRecords());
				event.getZipBuilder().add(json.getBytes(StandardCharsets.UTF_8))
						.path("db/" + CmsAdvertisement.TABLE_NAME + "/page" + pageNumber + ".json")
						.save();
				offset = page.getRecords().get(page.getRecords().size() - 1).getAdvertisementId();
				total -= page.getRecords().size();
			}
		}
	}
}
