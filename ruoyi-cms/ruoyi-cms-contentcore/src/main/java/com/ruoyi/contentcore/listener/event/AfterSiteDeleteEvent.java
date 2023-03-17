package com.ruoyi.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.ruoyi.contentcore.domain.CmsSite;

public class AfterSiteDeleteEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	
	private CmsSite site;
	
	public AfterSiteDeleteEvent(Object source, CmsSite site) {
		super(source);
		this.site = site;
	}

	public CmsSite getSite() {
		return site;
	}
}
