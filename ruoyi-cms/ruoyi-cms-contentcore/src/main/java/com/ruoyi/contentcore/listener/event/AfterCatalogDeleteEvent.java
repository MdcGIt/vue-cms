package com.ruoyi.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.ruoyi.contentcore.domain.CmsCatalog;

import lombok.Getter;

@Getter
public class AfterCatalogDeleteEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;

	private CmsCatalog catalog;
	
	public AfterCatalogDeleteEvent(Object source, CmsCatalog catalog) {
		super(source);
		this.catalog = catalog;
	}
}
