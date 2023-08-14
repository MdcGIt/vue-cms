package com.ruoyi.cms.comment.listener;

import com.ruoyi.cms.comment.properties.EnableCommentAudit;
import com.ruoyi.comment.fixed.dict.CommentAuditStatus;
import com.ruoyi.comment.listener.event.AfterCommentSubmitEvent;
import com.ruoyi.comment.listener.event.BeforeCommentSubmitEvent;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.service.impl.ContentDynamicDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommentEventListener {

	public static final String COMMENT_SOURCE_TYPE_CONTENT = "cms_content";

	private final ContentDynamicDataService contentDynamicDataService;

	private final IContentService contentService;

	private final ISiteService siteService;

	@EventListener
	public void beforeCommentSubmit(BeforeCommentSubmitEvent event) {
		if (!COMMENT_SOURCE_TYPE_CONTENT.equals(event.getComment().getSourceType())) {
			return;
		}
		Optional<CmsContent> opt = this.contentService.lambdaQuery()
				.select(CmsContent::getSiteId)
				.eq(CmsContent::getContentId, event.getComment().getSourceId())
				.oneOpt();
		if (opt.isEmpty()) {
			throw new RuntimeException("Comment content not found: " + event.getComment().getSourceId());
		}
		CmsSite site = this.siteService.getSite(opt.get().getSiteId());
		if (!EnableCommentAudit.getValue(site.getConfigProps())) {
			event.getComment().setAuditStatus(CommentAuditStatus.PASSED);
		}
	}

	@EventListener
	public void afterCommentSubmit(AfterCommentSubmitEvent event) {
		if (!COMMENT_SOURCE_TYPE_CONTENT.equals(event.getComment().getSourceType())) {
			return;
		}
		Long contentId = Long.valueOf(event.getComment().getSourceId());
		this.contentDynamicDataService.increaseCommentCount(contentId);
	}
}
