package com.ruoyi.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.SiteDTO;

public class AfterSiteSaveEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;
	
	private SiteDTO dto;
	
	private CmsSite site;
	
	public AfterSiteSaveEvent(Object source, CmsSite site, SiteDTO dto) {
		super(source);
		this.site = site;
		this.dto = dto;
	}

	public CmsSite getSite() {
		return this.site;
	}
	
	public SiteDTO getSiteDTO() {
		return this.dto;
	}
}
