package com.ruoyi.xmodel.core;

import com.ruoyi.xmodel.core.impl.MetaControlType_Input;
import com.ruoyi.xmodel.dto.FieldOptions;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 元数据模型类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IMetaModelType {

    String BEAN_PREFIX = "MetaModelType_";

    /**
     * 类型唯一标识
     */
    String getType();

    /**
     * 可用数据表名称固定前缀
     */
    String getTableNamePrefix();

    /**
     * 是否默认表
     */
    String getDefaultTable();

    /**
     * 模型数据表固定字段
     */
    default List<MetaModelField> getFixedFields() {
        return List.of(FIELD_MODEL_ID);
    }

    MetaModelField FIELD_MODEL_ID = new MetaModelField("模型ID", "modelId",
            "model_id", false, true, MetaControlType_Input.TYPE);
}
