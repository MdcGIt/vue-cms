package com.ruoyi.contentcore.job;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.mapper.CmsContentMapper;
import com.ruoyi.contentcore.properties.RecycleKeepDaysProperty;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 回收站内容过期回收
 */
@Slf4j
@RequiredArgsConstructor
@Component(RecycleExpireJobHandler.JOB_NAME)
public class RecycleExpireJobHandler extends IJobHandler {

	static final String JOB_NAME = "RecycleExpireJobHandler";

	private final ISiteService siteService;

	private final IContentService contentService;

	private final CmsContentMapper contentMapper;

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		log.info("Job start: {}", JOB_NAME);
		long s = System.currentTimeMillis();
		this.siteService.list().forEach(site -> {
			int days = RecycleKeepDaysProperty.getValue(site.getConfigProps());
			if (days > 0) {
				LocalDateTime expireTime = DateUtils.getDayStart(LocalDateTime.now().minusDays(days));
				long total = this.contentMapper.selectCountBeforeWithLogicDel(expireTime);
				int pageSize = 100;
				for (int i = 0; i * pageSize < total; i++) {
					log.debug("Job '{}' running: 【{}】 {} / {}", JOB_NAME, site.getName(), i * pageSize, total);
					try {
						Page<CmsContent> page = this.contentMapper.selectPageBeforeWithLogicDel(
								new Page<>(i, pageSize, false), expireTime);
						page.getRecords().forEach(rc -> {
							IContentType contentType = ContentCoreUtils.getContentType(rc.getContentType());
							contentType.deleteBackups(rc.getContentId());
						});
						List<Long> contentIds = page.getRecords().stream().map(CmsContent::getContentId).toList();
						contentMapper.deleteByIdsIgnoreLogicDel(contentIds);
					} catch (Exception e) {
						log.error("Job '{}' err: " + e.getMessage(), e);
					}
				}
			}
		});
		log.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}
}
