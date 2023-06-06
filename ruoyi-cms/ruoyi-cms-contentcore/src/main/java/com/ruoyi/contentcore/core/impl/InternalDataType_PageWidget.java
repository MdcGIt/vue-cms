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

/**
 * 内部数据类型：页面组件
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
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
	public String getPageData(RequestData data) throws IOException, TemplateException {
		CmsPageWidget pageWidget = pageWidgetService.getById(data.getDataId());
		Assert.notNull(pageWidget, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("pageWidgetId", data.getDataId()));
		
		return this.publishService.getPageWidgetPageData(pageWidget, data.isPreview());
	}
}
