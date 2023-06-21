package com.ruoyi.exmodel;

import com.ruoyi.exmodel.domain.CmsExtendModelData;
import com.ruoyi.xmodel.core.IMetaModelType;
import com.ruoyi.xmodel.core.MetaModelField;
import com.ruoyi.xmodel.core.impl.MetaControlType_Input;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 元数据模型类型：扩展模型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaModelType.BEAN_PREFIX + CmsExtendMetaModelType.TYPE)
public class CmsExtendMetaModelType implements IMetaModelType {

    public final static String TYPE = "CmsExtend";

    /**
     * 扩展模型数据字段键值前缀
     */
    public static final String DATA_FIELD_PREFIX = "ModelData_";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getTableNamePrefix() {
        return "cms_exd_";
    }
    @Override
    public String getDefaultTable() {
        return CmsExtendModelData.TABLE_NAME;
    }

    @Override
    public List<MetaModelField> getFixedFields() {
        return List.of(FIELD_MODEL_ID, FIELD_DATA_ID, FIELD_DATA_TYPE);
    }

    public static final MetaModelField FIELD_DATA_ID = new MetaModelField("数据ID", "dataId",
            "data_id", true, true, MetaControlType_Input.TYPE);

    public static final MetaModelField FIELD_DATA_TYPE = new MetaModelField("数据类型", "dataType",
            "data_type", true, true, MetaControlType_Input.TYPE);
}
