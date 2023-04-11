package com.ruoyi.link.listener;

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
}
