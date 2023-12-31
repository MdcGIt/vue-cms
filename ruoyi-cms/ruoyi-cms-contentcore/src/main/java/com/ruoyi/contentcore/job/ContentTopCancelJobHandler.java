package com.ruoyi.contentcore.job;

import java.time.LocalDateTime;

import com.ruoyi.system.schedule.IScheduledHandler;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 内容置顶取消任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + ContentTopCancelJobHandler.JOB_NAME)
public class ContentTopCancelJobHandler extends IJobHandler implements IScheduledHandler {

	static final String JOB_NAME = "ContentTopCancelJobHandler";

	private final IContentService contentService;

	@Override
	public String getId() {
		return JOB_NAME;
	}

	@Override
	public String getName() {
		return "{SCHEDULED_TASK." + JOB_NAME + "}";
	}

	@Override
	public void exec() throws Exception {
		log.info("Job start: {}", JOB_NAME);
		long s = System.currentTimeMillis();
		int pageSize = 500;
		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
				.gt(CmsContent::getTopFlag, 0)
				.le(CmsContent::getTopDate, LocalDateTime.now());
		long total = contentService.count(q);
		for (int i = 0; i * pageSize < total; i++) {
			log.debug("Job '{}' running: {} / {}", JOB_NAME, i * 100, total);
			Page<CmsContent> page = contentService.page(new Page<>(i, pageSize, false), q);
			for (CmsContent xContent : page.getRecords()) {
				IContentType ct = ContentCoreUtils.getContentType(xContent.getContentType());
				IContent<?> content = ct.loadContent(xContent);
				content.cancelTop();
			}
		}
		log.info("Job '{}' completed, cost: {}ms", JOB_NAME, System.currentTimeMillis() - s);
	}

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		this.exec();
	}
}
