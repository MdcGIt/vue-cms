package com.ruoyi.contentcore.core.impl;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.service.IPageWidgetService;
import com.ruoyi.contentcore.service.IPublishService;

import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(IInternalDataType.BEAN_NAME_PREFIX + InternalDataType_PageWidget.ID)
public class InternalDataType_PageWidget implements IInternalDataType {

	public final static String ID = "pagewidget";
	
	private final IPageWidgetService pageWidgetService;
	
	private final IPublishService publishService;
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getPageData(Long dataId, int pageIndex, String publishPipeCode, boolean isPreview) throws IOException, TemplateException {
		CmsPageWidget pageWidget = pageWidgetService.getById(dataId);
		Assert.notNull(pageWidget, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("pageWidgetId", dataId));
		
		return this.publishService.getPageWidgetPageData(pageWidget, isPreview);
	}
}
