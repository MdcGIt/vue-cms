package com.ruoyi.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.ruoyi.contentcore.core.IContent;

import lombok.Getter;

@Getter
public class AfterContentDeleteEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;
	
	private IContent<?> content;
	
	public AfterContentDeleteEvent(Object source, IContent<?> content) {
		super(source);
		this.content = content;
	}
}
