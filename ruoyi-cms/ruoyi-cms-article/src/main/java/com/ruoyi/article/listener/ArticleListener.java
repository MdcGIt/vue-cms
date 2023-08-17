package com.ruoyi.article.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.article.domain.CmsArticleDetail;
import com.ruoyi.article.domain.vo.ArticleVO;
import com.ruoyi.article.mapper.CmsArticleDetailMapper;
import com.ruoyi.article.service.IArticleService;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.listener.event.AfterContentEditorInitEvent;
import com.ruoyi.contentcore.listener.event.BeforeSiteDeleteEvent;
import com.ruoyi.contentcore.listener.event.SiteExportEvent;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ArticleListener {

	private final IArticleService articleService;

	private final CmsArticleDetailMapper articleMapper;

	@EventListener
	public void beforeSiteDelete(BeforeSiteDeleteEvent event) {
		CmsSite site = event.getSite();
		int pageSize = 500;
		// 删除文章数据
		try {
			long total = this.articleMapper.selectCountBySiteIdIgnoreLogicDel(site.getSiteId());
			for (int i = 0; i * pageSize < total; i++) {
				AsyncTaskManager.setTaskProgressInfo((int)  (i * pageSize * 100 / total), "正在删除文章详情备份数据：" + (i * pageSize) + "/" + total);
				this.articleMapper.deleteBySiteIdIgnoreLogicDel(site.getSiteId(), pageSize);
			}
		} catch (Exception e) {
			e.printStackTrace();
			AsyncTaskManager.addErrMessage("删除文章详情错误：" + e.getMessage());
		}
	}

	@EventListener
	public void afterContentEditorInit(AfterContentEditorInitEvent event) {
		if (event.getContentVO() instanceof ArticleVO vo) {
			vo.setContentHtml(InternalUrlUtils.dealResourceInternalUrl(vo.getContentHtml()));
		}
	}

	@EventListener
	public void onSiteExport(SiteExportEvent event) throws IOException {
		// cms_article_detail
		long total = articleService.count(new LambdaQueryWrapper<CmsArticleDetail>()
				.eq(CmsArticleDetail::getSiteId, event.getSite().getSiteId()));
		int pageNumber = 1;
		long offset = 0;
		long limit = 500;
		while (total > 0) {
			LambdaQueryWrapper<CmsArticleDetail> q = new LambdaQueryWrapper<CmsArticleDetail>()
					.eq(CmsArticleDetail::getSiteId, event.getSite().getSiteId())
					.gt(CmsArticleDetail::getContentId, offset)
					.orderByAsc(CmsArticleDetail::getContentId);
			Page<CmsArticleDetail> page = articleService.page(new Page<>(1, limit, false), q);
			String json = JacksonUtils.to(page.getRecords());
			event.getZipBuilder().add(json.getBytes(StandardCharsets.UTF_8))
					.path("db/" + CmsArticleDetail.TABLE_NAME + "/page" + pageNumber + ".json")
					.save();
			offset = page.getRecords().get(page.getRecords().size() - 1).getContentId();
			total -= page.getRecords().size();
		}
	}
}
