package com.ruoyi.exmodel.service;

import com.ruoyi.common.utils.NumberUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.exmodel.CmsExtendMetaModelType;
import com.ruoyi.exmodel.domain.dto.XModelFieldDataDTO;
import com.ruoyi.exmodel.fixed.dict.ExtendModelDataType;
import com.ruoyi.exmodel.properties.ContentExtendModelProperty;
import com.ruoyi.xmodel.core.MetaModel;
import com.ruoyi.xmodel.core.impl.MetaControlType_Checkbox;
import com.ruoyi.xmodel.domain.XModelField;
import com.ruoyi.xmodel.service.IModelDataService;
import com.ruoyi.xmodel.service.IModelFieldService;
import com.ruoyi.xmodel.service.IModelService;
import com.ruoyi.xmodel.util.XModelUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class ExModelService {

    private final IModelService modelService;

    private final IModelDataService modelDataService;

    private final ICatalogService catalogService;

    public List<XModelFieldDataDTO> getModelData(CmsContent content) {

        CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
        String modelId = ContentExtendModelProperty.getValue(catalog.getConfigProps());
        if (!NumberUtils.isCreatable(modelId)) {
            return List.of();
        }
        return this.getModelData(Long.valueOf(modelId), ExtendModelDataType.CONTENT, content.getContentId().toString());
    }

    public List<XModelFieldDataDTO> getModelData(Long modelId, String dataType, String dataId) {
        MetaModel metaModel = this.modelService.getMetaModel(modelId);
        if (Objects.isNull(metaModel)) {
            return List.of();
        }

        Map<String, Object> data = this.modelDataService.getModelDataByPkValue(modelId,
                Map.of(
                        CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId,
                        CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), dataType,
                        CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId
                ));

        List<XModelFieldDataDTO> list = new ArrayList<>();
        metaModel.getFields().forEach(f -> {
            String fv = MapUtils.getString(data, f.getCode(), f.getDefaultValue());

            XModelFieldDataDTO dto = new XModelFieldDataDTO();
            dto.setLabel(f.getName());
            dto.setFieldName(CmsExtendMetaModelType.DATA_FIELD_PREFIX + f.getCode());
            dto.setControlType(f.getControlType());
            dto.setValue(fv);
            dto.setOptions(XModelUtils.getOptions(f.getOptions()));
            if (MetaControlType_Checkbox.TYPE.equals(dto.getControlType())) {
                if (dto.getValue() == null || StringUtils.isBlank(dto.getValue().toString())) {
                    dto.setValue(new String[0]);
                } else {
                    String[] value = StringUtils.split(dto.getValue().toString(), StringUtils.COMMA);
                    dto.setValue(value);
                }
            }
            list.add(dto);
        });
        return list;
    }
}
