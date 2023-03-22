package com.ruoyi.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.SiteDTO;

import lombok.Getter;

@Getter
public class AfterSiteSaveEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;
	
	private SiteDTO siteDTO;
	
	private CmsSite site;
	
	public AfterSiteSaveEvent(Object source, CmsSite site, SiteDTO dto) {
		super(source);
		this.site = site;
		this.siteDTO = dto;
	}
}
