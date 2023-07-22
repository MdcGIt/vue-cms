package com.ruoyi.cms.comment.listener;

import com.ruoyi.comment.listener.event.AfterCommentSubmitEvent;
import com.ruoyi.contentcore.service.impl.ContentDynamicDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentEventListener {

	public static final String COMMENT_SOURCE_TYPE_CONTENT = "CMS.CONTENT";

	private final ContentDynamicDataService contentDynamicDataService;

	@EventListener
	public void afterCommentSubmit(AfterCommentSubmitEvent event) {
		if (!COMMENT_SOURCE_TYPE_CONTENT.equals(event.getComment().getSourceType())) {
			return;
		}
		Long contentId = Long.valueOf(event.getComment().getSourceId());
		this.contentDynamicDataService.increaseCommentCount(contentId);
	}
}
