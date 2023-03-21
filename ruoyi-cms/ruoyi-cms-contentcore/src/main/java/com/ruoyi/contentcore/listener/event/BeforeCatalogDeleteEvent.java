package com.ruoyi.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.ruoyi.contentcore.domain.CmsCatalog;

public class BeforeCatalogDeleteEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;

	private CmsCatalog catalog;
	
	public BeforeCatalogDeleteEvent(Object source, CmsCatalog catalog) {
		super(source);
		this.catalog = catalog;
	}

	public CmsCatalog getCatalog() {
		return this.catalog;
	}
}
