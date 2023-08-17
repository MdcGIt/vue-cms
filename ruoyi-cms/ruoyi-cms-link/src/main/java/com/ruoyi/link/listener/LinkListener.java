package com.ruoyi.link.listener;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.contentcore.listener.event.SiteExportEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.ruoyi.link.domain.CmsLink;
import com.ruoyi.link.domain.CmsLinkGroup;
import com.ruoyi.link.service.ILinkGroupService;
import com.ruoyi.link.service.ILinkService;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class LinkListener {

	private final ILinkGroupService linkGroupService;

	private final ILinkService linkService;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		try {
			// 删除友链数据
			long total = this.linkService
					.count(new LambdaQueryWrapper<CmsLink>().eq(CmsLink::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除友链数据：" + (i * pageSize) + "/" + total);
				this.linkService.remove(new LambdaQueryWrapper<CmsLink>().eq(CmsLink::getSiteId, site.getSiteId())
						.last("limit " + pageSize));
			}
			// 删除友链分组数据
			total = this.linkGroupService
					.count(new LambdaQueryWrapper<CmsLinkGroup>().eq(CmsLinkGroup::getSiteId, site.getSiteId()));
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize * 100 / total),
						"正在删除友链分组数据：" + (i * pageSize) + "/" + total);
				this.linkGroupService.remove(new LambdaQueryWrapper<CmsLinkGroup>()
						.eq(CmsLinkGroup::getSiteId, site.getSiteId()).last("limit " + pageSize));
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除友链数据错误：" + e.getMessage());
		}
	}

	@EventListener
	public void onSiteExport(SiteExportEvent event) throws IOException {
		// cms_link_group
		{
			long total = linkGroupService.count(new LambdaQueryWrapper<CmsLinkGroup>()
					.eq(CmsLinkGroup::getSiteId, event.getSite().getSiteId()));
			int pageNumber = 1;
			long offset = 0;
			long limit = 500;
			while (total > 0) {
				LambdaQueryWrapper<CmsLinkGroup> q = new LambdaQueryWrapper<CmsLinkGroup>()
						.eq(CmsLinkGroup::getSiteId, event.getSite().getSiteId())
						.gt(CmsLinkGroup::getLinkGroupId, offset)
						.orderByAsc(CmsLinkGroup::getLinkGroupId);
				Page<CmsLinkGroup> page = linkGroupService.page(new Page<>(1, limit, false), q);
				String json = JacksonUtils.to(page.getRecords());
				event.getZipBuilder().add(json.getBytes(StandardCharsets.UTF_8))
						.path("db/" + CmsLinkGroup.TABLE_NAME + "/page" + pageNumber + ".json")
						.save();
				offset = page.getRecords().get(page.getRecords().size() - 1).getLinkGroupId();
				total -= page.getRecords().size();
			}
		}
		// cms_link
		{
			long total = linkService.count(new LambdaQueryWrapper<CmsLink>()
					.eq(CmsLink::getSiteId, event.getSite().getSiteId()));
			int pageNumber = 1;
			long offset = 0;
			long limit = 500;
			while (total > 0) {
				LambdaQueryWrapper<CmsLink> q = new LambdaQueryWrapper<CmsLink>()
						.eq(CmsLink::getSiteId, event.getSite().getSiteId())
						.gt(CmsLink::getLinkId, offset)
						.orderByAsc(CmsLink::getLinkId);
				Page<CmsLink> page = linkService.page(new Page<>(1, limit, false), q);
				String json = JacksonUtils.to(page.getRecords());
				event.getZipBuilder().add(json.getBytes(StandardCharsets.UTF_8))
						.path("db/" + CmsLink.TABLE_NAME + "/page" + pageNumber + ".json")
						.save();
				offset = page.getRecords().get(page.getRecords().size() - 1).getLinkId();
				total -= page.getRecords().size();
			}
		}
	}
}
