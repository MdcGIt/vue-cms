package com.ruoyi.xmodel.core.impl;

import com.ruoyi.xmodel.core.IMetaControlType;
import org.springframework.stereotype.Component;

/**
 * 元数据模型字段类型：多选框
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IMetaControlType.BEAN_PREFIX + MetaControlType_Checkbox.TYPE)
public class MetaControlType_Checkbox implements IMetaControlType {

    public static final String TYPE = "checkbox";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "{META.CONTROL_TYPE." + TYPE + "}";
    }
}
