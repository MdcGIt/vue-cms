package com.ruoyi.contentcore.listener.event;

import java.util.Map;

import org.springframework.context.ApplicationEvent;

import com.ruoyi.contentcore.domain.CmsCatalog;

public class AfterCatalogSaveEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;

	private String oldPath;
	
	private Map<String, Object> extendParams;

	private CmsCatalog catalog;
	
	public AfterCatalogSaveEvent(Object source, CmsCatalog catalog, String oldPath, Map<String, Object> extendParams) {
		super(source);
		this.catalog = catalog;
		this.oldPath = oldPath;
		this.extendParams = extendParams;
	}

	public CmsCatalog getCatalog() {
		return this.catalog;
	}
	
	public String getOldPath() {
		return this.oldPath;
	}

	public Map<String, Object> getExtendParams() {
		return extendParams;
	}
}
