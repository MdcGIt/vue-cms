package com.ruoyi.exmodel.listener;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.NumberUtils;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.vo.ContentVO;
import com.ruoyi.contentcore.listener.event.AfterCatalogDeleteEvent;
import com.ruoyi.contentcore.listener.event.AfterCatalogSaveEvent;
import com.ruoyi.contentcore.listener.event.AfterContentDeleteEvent;
import com.ruoyi.contentcore.listener.event.AfterContentEditorInitEvent;
import com.ruoyi.contentcore.listener.event.AfterContentSaveEvent;
import com.ruoyi.contentcore.listener.event.AfterSiteDeleteEvent;
import com.ruoyi.contentcore.listener.event.AfterSiteSaveEvent;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.exmodel.properties.CatalogExtendModelProperty;
import com.ruoyi.exmodel.properties.ContentExtendModelProperty;
import com.ruoyi.exmodel.properties.SiteExtendModelProperty;
import com.ruoyi.xmodel.XModelUtils;
import com.ruoyi.xmodel.service.IModelDataService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EXModelEventListener {

	private final IModelDataService modelDataService;
	
	private final ICatalogService catalogService;
	
	@EventListener
	public void afterContentEditorInit(AfterContentEditorInitEvent event) {
		ContentVO vo = event.getContentVO();
		CmsCatalog catalog = this.catalogService.getCatalog(vo.getCatalogId());
		String modelId = ContentExtendModelProperty.getValue(catalog.getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			vo.getCatalogConfigProps().put(ContentExtendModelProperty.ID, modelId);
		}
	}
	
	@EventListener
	public void afterSiteSave(AfterSiteSaveEvent event) {
		String modelId = SiteExtendModelProperty.getValue(event.getSite().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String pkValue = event.getSite().getSiteId().toString();
			this.saveModelData(Long.valueOf(modelId), pkValue, event.getSiteDTO().getParams());
		}
	}
	
	@EventListener
	public void afterSiteDelete(AfterSiteDeleteEvent event) {
		String modelId = SiteExtendModelProperty.getValue(event.getSite().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String pkValue = event.getSite().getSiteId().toString();
			this.modelDataService.deleteModelData(Long.valueOf(modelId), pkValue);
		}
	}
	
	@EventListener
	public void afterCatalogSave(AfterCatalogSaveEvent event) {
		String modelId =	CatalogExtendModelProperty.getValue(event.getCatalog().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String pkValue = event.getCatalog().getCatalogId().toString();
			this.saveModelData(Long.valueOf(modelId), pkValue, event.getExtendParams());
		}
	}
	
	@EventListener
	public void afterCatalogDelete(AfterCatalogDeleteEvent event) {
		String modelId = CatalogExtendModelProperty.getValue(event.getCatalog().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String pkValue = event.getCatalog().getCatalogId().toString();
			this.modelDataService.deleteModelData(Long.valueOf(modelId), pkValue);
		}
	}
	
	@EventListener
	public void afterContentSave(AfterContentSaveEvent event) {
		IContent<?> content = event.getContent();
		String modelId = ContentExtendModelProperty.getValue(content.getCatalog().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String pkValue = String.valueOf(content.getContentEntity().getContentId());
			this.saveModelData(Long.valueOf(modelId), pkValue, content.getParams());
		}
	}

	@EventListener
	public void afterContentDelete(AfterContentDeleteEvent event) {
		IContent<?> content = event.getContent();
		String modelId = ContentExtendModelProperty.getValue(content.getCatalog().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String pkValue = String.valueOf(content.getContentEntity().getContentId());
			this.modelDataService.deleteModelData(Long.valueOf(modelId), pkValue);
		}
	}
	
	private void saveModelData(Long modelId, String pkValue, Map<String, Object> params) {
		Map<String, Object> datas = new HashMap<>();
		params.entrySet().forEach(e -> {
			if (e.getKey().startsWith(XModelUtils.DATA_FIELD_PREFIX)) {
				datas.put(e.getKey().substring(XModelUtils.DATA_FIELD_PREFIX.length()), e.getValue());
			}
		});
		this.modelDataService.saveModelData(modelId, pkValue, datas);
	}
}
