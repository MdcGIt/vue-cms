package com.ruoyi.contentcore.job;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.fixed.dict.ContentStatus;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.IPublishService;
import com.ruoyi.contentcore.service.ISiteService;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 定时发布任务<br/>
 * 
 * 仅发布待发布状态且发布时间为空或发布时间在当前时间之前的内容
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SitePublishJobHandler extends IJobHandler {
	
	static final String JOB_NAME = "SitePublishJobHandler";

	private final ISiteService siteService;
	
	private final ICatalogService catalogService;
	
	private final IContentService contentService;
	
	private final IPublishService publishService;

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		log.info("Job start: {}", JOB_NAME);
		long s = System.currentTimeMillis();
		List<CmsSite> sites = this.siteService.list();
		for (CmsSite site : sites) {
			List<CmsCatalog> catalogList = catalogService
					.list(new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId, site.getSiteId()));
			for (CmsCatalog catalog : catalogList) {
				// 先发布内容
				int pageSize = 500;
				LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
						.eq(CmsContent::getCatalogId, catalog.getCatalogId())
						.eq(CmsContent::getStatus, ContentStatus.TO_PUBLISHED)
						.le(CmsContent::getPublishDate, LocalDateTime.now());
				long total = contentService.count(q);
				for (int i = 0; i * pageSize < total; i++) {
					Page<CmsContent> page = contentService.page(new Page<>(i, pageSize, false), q);
					for (CmsContent xContent : page.getRecords()) {
						this.publishService.contentStaticize(xContent);
					}
				}
			}
			// 发布栏目
			for (int i = 0; i < catalogList.size(); i++) {
				CmsCatalog catalog = catalogList.get(i);
				this.publishService.catalogStaticize(catalog, -1);
			}
			// 发布站点
			this.publishService.siteStaticize(site);
		}
		log.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}
}
