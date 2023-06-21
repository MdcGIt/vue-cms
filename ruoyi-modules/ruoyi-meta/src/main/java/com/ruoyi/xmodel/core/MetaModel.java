package com.ruoyi.xmodel.core;

import com.ruoyi.xmodel.domain.XModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 元数据模型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class MetaModel {

    private XModel model;

    private List<MetaModelField> fields;
}
