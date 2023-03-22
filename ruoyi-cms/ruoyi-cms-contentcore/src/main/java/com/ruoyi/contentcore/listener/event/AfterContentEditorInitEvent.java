package com.ruoyi.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.ruoyi.contentcore.domain.vo.ContentVO;

import lombok.Getter;

@Getter
public class AfterContentEditorInitEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;

	private ContentVO contentVO;
	
	public AfterContentEditorInitEvent(Object source, ContentVO vo) {
		super(source);
		this.contentVO = vo;
	}
}
