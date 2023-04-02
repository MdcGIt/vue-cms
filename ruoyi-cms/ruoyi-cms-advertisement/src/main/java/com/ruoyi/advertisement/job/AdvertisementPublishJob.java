package com.ruoyi.advertisement.job;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.advertisement.AdSpacePageWidgetType;
import com.ruoyi.advertisement.domain.CmsAdvertisement;
import com.ruoyi.advertisement.service.IAdvertisementService;
import com.ruoyi.contentcore.core.IPageWidgetType;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.fixed.dict.PageWidgetStatus;
import com.ruoyi.contentcore.service.IPageWidgetService;
import com.ruoyi.contentcore.service.IPublishService;
import com.ruoyi.system.fixed.dict.EnableOrDisable;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 广告定时发布任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdvertisementPublishJob extends IJobHandler {
	
	static final String JOB_NAME = "AdvertisementPublishJob";

	private final IPageWidgetService pageWidgetService;

	private final IAdvertisementService advertisementService;
	
	private final IPublishService publishService;

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		log.info("AdvertisementPublishJob start");
		long s = System.currentTimeMillis();
		LocalDateTime now = LocalDateTime.now();
		List<CmsPageWidget> list = this.pageWidgetService.list(new LambdaQueryWrapper<CmsPageWidget>()
				.eq(CmsPageWidget::getState, PageWidgetStatus.PUBLISHED)
				.eq(CmsPageWidget::getType, AdSpacePageWidgetType.ID));
		for (CmsPageWidget adSpace : list) {
			boolean changed = false;
			List<CmsAdvertisement> toOnlineList = this.advertisementService.list(new LambdaQueryWrapper<CmsAdvertisement>()
					.eq(CmsAdvertisement::getState, EnableOrDisable.DISABLE)
					.eq(CmsAdvertisement::getAdSpaceId, adSpace.getPageWidgetId())
					.le(CmsAdvertisement::getOnlineDate, now)
					.ge(CmsAdvertisement::getOfflineDate, now));
			if (toOnlineList != null && toOnlineList.size() > 0) {
				changed = true;
				for (CmsAdvertisement ad : toOnlineList) {
					ad.setState(EnableOrDisable.ENABLE);
				}
				this.advertisementService.updateBatchById(toOnlineList);
			}
			// 下线时间小于当前时间的启用广告标记为停用
			List<CmsAdvertisement> toOfflineList = this.advertisementService.list(new LambdaQueryWrapper<CmsAdvertisement>()
					.eq(CmsAdvertisement::getState, EnableOrDisable.ENABLE)
					.eq(CmsAdvertisement::getAdSpaceId, adSpace.getPageWidgetId())
					.lt(CmsAdvertisement::getOfflineDate, now));
			if (toOfflineList != null && toOfflineList.size() > 0) {
				changed = true;
				for (CmsAdvertisement ad : toOfflineList) {
					ad.setState(EnableOrDisable.DISABLE);
				}
				this.advertisementService.updateBatchById(toOfflineList);
			}
			// 有变化重新发布广告版位
			if (changed) {
				IPageWidgetType pwt = this.pageWidgetService.getPageWidgetType(adSpace.getType());
				this.publishService.pageWidgetStaticize(pwt.loadPageWidget(adSpace));
			}
		}
		log.info("AdvertisementPublishJob completed, cost: {}ms", System.currentTimeMillis() - s);
	}
}
