package com.ruoyi.contentcore.listener.event;

import org.springframework.context.ApplicationEvent;

import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.contentcore.domain.CmsCatalog;

import lombok.Getter;

@Getter
public class BeforeCatalogDeleteEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;

	private CmsCatalog catalog;
	
	private LoginUser operator;
	
	public BeforeCatalogDeleteEvent(Object source, CmsCatalog catalog, LoginUser operator) {
		super(source);
		this.catalog = catalog;
		this.operator = operator;
	}
}
