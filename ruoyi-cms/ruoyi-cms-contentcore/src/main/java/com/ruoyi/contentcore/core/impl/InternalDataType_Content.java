package com.ruoyi.contentcore.core.impl;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.core.InternalURL;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.IPublishService;

import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(IInternalDataType.BEAN_NAME_PREFIX + InternalDataType_Content.ID)
public class InternalDataType_Content implements IInternalDataType {

	public final static String ID = "content";
	
	private final IContentService contentService;
	
	private final IPublishService publishService;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getPageData(Long dataId, int pageIndex, String publishPipeCode, boolean isPreview) throws IOException, TemplateException {
		CmsContent content = contentService.getById(dataId);
		Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", dataId));
		
		return this.publishService.getContentPageData(content, pageIndex, publishPipeCode, isPreview);
	}

	@Override
	public String getLink(InternalURL internalUrl, int pageIndex, String publishPipeCode, boolean isPreview) {
		CmsContent content = contentService.getById(internalUrl.getId());
		Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", internalUrl.getId()));
		
		return this.contentService.getContentLink(content, 1, publishPipeCode, isPreview);
	}
}
