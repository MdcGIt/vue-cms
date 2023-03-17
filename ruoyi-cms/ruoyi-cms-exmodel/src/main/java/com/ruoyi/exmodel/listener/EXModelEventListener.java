package com.ruoyi.exmodel.listener;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.vo.ContentVO;
import com.ruoyi.contentcore.listener.event.AfterCatalogSaveEvent;
import com.ruoyi.contentcore.listener.event.AfterContentEditorInitEvent;
import com.ruoyi.contentcore.listener.event.AfterContentSaveEvent;
import com.ruoyi.contentcore.listener.event.AfterSiteSaveEvent;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.exmodel.properties.ContentExtendModelProperty;
import com.ruoyi.exmodel.properties.ExtendModelProperty;
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
		vo.getCatalogConfigProps().put(ContentExtendModelProperty.ID, modelId);
	}
	
	@EventListener
	public void afterSiteSave(AfterSiteSaveEvent event) {
		String modelId = ExtendModelProperty.getValue(event.getSite().getConfigProps());
		String pkValue = String.valueOf(event.getSite().getSiteId());
		this.saveModelData(modelId, pkValue, event.getSiteDTO().getParams());
	}
	
	@EventListener
	public void afterCatalogSave(AfterCatalogSaveEvent event) {
		String modelId = ExtendModelProperty.getValue(event.getCatalog().getConfigProps());
		String pkValue = String.valueOf(event.getCatalog().getCatalogId());
		this.saveModelData(modelId, pkValue, event.getExtendParams());
	}
	
	@EventListener
	public void afterContentSave(AfterContentSaveEvent event) {
		IContent<?> content = event.getContent();
		String modelId = ContentExtendModelProperty.getValue(content.getCatalog().getConfigProps());
		String pkValue = String.valueOf(content.getContentEntity().getContentId());
		this.saveModelData(modelId, pkValue, content.getParams());
	}
	
	private void saveModelData(String modelId, String pkValue, Map<String, Object> params) {
		if (StringUtils.isEmpty(modelId)) {
			return;
		}
		Map<String, Object> datas = new HashMap<>();
		params.entrySet().forEach(e -> {
			if (e.getKey().startsWith(XModelUtils.DATA_FIELD_PREFIX)) {
				datas.put(e.getKey().substring(XModelUtils.DATA_FIELD_PREFIX.length()), e.getValue());
			}
		});
		this.modelDataService.saveModelData(Long.valueOf(modelId), pkValue, datas);
	}
}
