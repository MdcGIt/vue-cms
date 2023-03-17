package com.ruoyi.article.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ruoyi.article.domain.vo.ArticleVO;
import com.ruoyi.contentcore.listener.event.AfterContentEditorInitEvent;
import com.ruoyi.contentcore.util.InternalUrlUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ArticleListener {

	@EventListener
	public void before(AfterContentEditorInitEvent event) {
		if (event.getContentVO() instanceof ArticleVO vo) {
			vo.setContentHtml(InternalUrlUtils.dealResourceInternalUrl(vo.getContentHtml()));
		}
	}
}
