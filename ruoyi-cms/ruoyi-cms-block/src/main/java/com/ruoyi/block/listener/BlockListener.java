package com.ruoyi.block.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.block.ManualPageWidgetType;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.listener.event.SiteExportEvent;
import com.ruoyi.contentcore.service.IPageWidgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class BlockListener {

	private final IPageWidgetService pageWidgetService;

	@EventListener
	public void onSiteExport(SiteExportEvent event) throws IOException {
		// cms_page_widget
		long total = pageWidgetService.count(new LambdaQueryWrapper<CmsPageWidget>()
				.eq(CmsPageWidget::getSiteId, event.getSite().getSiteId())
				.eq(CmsPageWidget::getType, ManualPageWidgetType.ID));
		int pageNumber = 1;
		long offset = 0;
		long limit = 500;
		while (total > 0) {
			LambdaQueryWrapper<CmsPageWidget> q = new LambdaQueryWrapper<CmsPageWidget>()
					.eq(CmsPageWidget::getSiteId, event.getSite().getSiteId())
					.eq(CmsPageWidget::getType, ManualPageWidgetType.ID)
					.gt(CmsPageWidget::getPageWidgetId, offset)
					.orderByAsc(CmsPageWidget::getPageWidgetId);
			Page<CmsPageWidget> page = pageWidgetService.page(new Page<>(1, limit, false), q);
			String json = JacksonUtils.to(page.getRecords());
			event.getZipBuilder().add(json.getBytes(StandardCharsets.UTF_8))
					.path("db/" + CmsPageWidget.TABLE_NAME + "-" + ManualPageWidgetType.ID + "/page" + pageNumber + ".json")
					.save();
			offset = page.getRecords().get(page.getRecords().size() - 1).getPageWidgetId();
			total -= page.getRecords().size();
		}
	}
}
