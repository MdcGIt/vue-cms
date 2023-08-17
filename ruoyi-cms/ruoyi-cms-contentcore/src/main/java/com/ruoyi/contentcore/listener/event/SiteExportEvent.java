package com.ruoyi.contentcore.listener.event;

import com.ruoyi.contentcore.domain.CmsSite;
import jodd.io.ZipBuilder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SiteExportEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private CmsSite site;

	private ZipBuilder zipBuilder;

	public SiteExportEvent(Object source, CmsSite site, ZipBuilder zipBuilder) {
		super(source);
		this.site = site;
		this.zipBuilder = zipBuilder;
	}
}
