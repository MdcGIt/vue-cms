package com.ruoyi.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.ruoyi.contentcore.domain.vo.ContentVO;

public class AfterContentEditorInitEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;

	private ContentVO vo;
	
	public AfterContentEditorInitEvent(Object source, ContentVO vo) {
		super(source);
		this.vo = vo;
	}

	public ContentVO getContentVO() {
		return vo;
	}
}
