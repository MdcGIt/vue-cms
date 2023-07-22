package com.ruoyi.exmodel.listener;

import com.ruoyi.common.utils.NumberUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.vo.ContentVO;
import com.ruoyi.contentcore.listener.event.*;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.exmodel.CmsExtendMetaModelType;
import com.ruoyi.exmodel.domain.CmsExtendModelData;
import com.ruoyi.exmodel.fixed.dict.ExtendModelDataType;
import com.ruoyi.exmodel.properties.CatalogExtendModelProperty;
import com.ruoyi.exmodel.properties.ContentExtendModelProperty;
import com.ruoyi.exmodel.properties.SiteExtendModelProperty;
import com.ruoyi.xmodel.service.IModelDataService;
import com.ruoyi.xmodel.util.XModelUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			String dataId = event.getSite().getSiteId().toString();
			this.saveModelData(Long.valueOf(modelId), ExtendModelDataType.SITE, dataId, event.getSiteDTO().getParams());
		}
	}

	@EventListener
	public void afterSiteDelete(AfterSiteDeleteEvent event) {
		String modelId = SiteExtendModelProperty.getValue(event.getSite().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String dataId = event.getSite().getSiteId().toString();
			this.modelDataService.deleteModelDataByPkValue(Long.valueOf(modelId),
					List.of(Map.of(
							CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId,
							CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), ExtendModelDataType.SITE,
							CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId
					)));
		}
	}

	@EventListener
	public void afterCatalogSave(AfterCatalogSaveEvent event) {
		String modelId = CatalogExtendModelProperty.getValue(event.getCatalog().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String dataId = event.getCatalog().getCatalogId().toString();
			this.saveModelData(Long.valueOf(modelId), ExtendModelDataType.CATALOG, dataId, event.getExtendParams());
		}
	}

	@EventListener
	public void afterCatalogDelete(AfterCatalogDeleteEvent event) {
		String modelId = CatalogExtendModelProperty.getValue(event.getCatalog().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String dataId = event.getCatalog().getCatalogId().toString();
			this.modelDataService.deleteModelDataByPkValue(Long.valueOf(modelId),
					List.of(Map.of(
							CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId,
							CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), ExtendModelDataType.CATALOG,
							CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId
					)));
		}
	}

	@EventListener
	public void afterContentSave(AfterContentSaveEvent event) {
		IContent<?> content = event.getContent();
		String modelId = ContentExtendModelProperty.getValue(content.getCatalog().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String dataId = String.valueOf(content.getContentEntity().getContentId());
			this.saveModelData(Long.valueOf(modelId), ExtendModelDataType.CONTENT, dataId, content.getParams());
		}
	}

	@EventListener
	public void afterContentDelete(AfterContentDeleteEvent event) {
		IContent<?> content = event.getContent();
		String modelId = ContentExtendModelProperty.getValue(content.getCatalog().getConfigProps());
		if (NumberUtils.isDigits(modelId)) {
			String dataId = String.valueOf(content.getContentEntity().getContentId());
			this.modelDataService.deleteModelDataByPkValue(Long.valueOf(modelId),
					List.of(Map.of(
							CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId,
							CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), ExtendModelDataType.CONTENT,
							CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId
					)));
		}
	}

	private void saveModelData(Long modelId, String dataType, String dataId, Map<String, Object> params) {
		Map<String, Object> dataMap = new HashMap<>();
		params.entrySet().forEach(e -> {
			String fieldCode = e.getKey();
			if (fieldCode.startsWith(CmsExtendMetaModelType.DATA_FIELD_PREFIX)) {
				fieldCode = StringUtils.substringAfter(e.getKey(), CmsExtendMetaModelType.DATA_FIELD_PREFIX);
			}
			dataMap.put(fieldCode, e.getValue());
		});
		dataMap.put(CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId);
		dataMap.put(CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), dataType);
		dataMap.put(CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId);
		this.modelDataService.saveModelData(modelId, dataMap);
	}
}
