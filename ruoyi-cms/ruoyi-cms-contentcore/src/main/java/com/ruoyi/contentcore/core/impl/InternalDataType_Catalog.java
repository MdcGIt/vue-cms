package com.ruoyi.contentcore.core.impl;

import java.io.IOException;

import com.ruoyi.contentcore.util.CatalogUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;
import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.core.InternalURL;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IPublishService;

import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

/**
 * 内部数据类型：栏目
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IInternalDataType.BEAN_NAME_PREFIX + InternalDataType_Catalog.ID)
public class InternalDataType_Catalog implements IInternalDataType {

	public final static String ID = "catalog";
	
	private final ICatalogService catalogService;
	
	private final IPublishService publishService;
	
	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public String getPageData(RequestData requestData) throws IOException, TemplateException {
		CmsCatalog catalog = catalogService.getCatalog(requestData.getDataId());
		boolean listFlag = YesOrNo.isYes(requestData.getParams().get("list"));
		return this.publishService.getCatalogPageData(catalog, requestData.getPageIndex(), listFlag, requestData.getPublishPipeCode(), requestData.isPreview());
	}

	@Override
	public String getLink(InternalURL internalUrl, int pageIndex, String publishPipeCode, boolean isPreview) {
		CmsCatalog catalog = catalogService.getCatalog(internalUrl.getId());
		return this.catalogService.getCatalogLink(catalog, pageIndex, publishPipeCode, isPreview);
	}
}
