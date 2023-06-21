package com.ruoyi.exmodel.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.xmodel.core.BaseModelData;
import lombok.Getter;
import lombok.Setter;

/**
 * 扩展模型数据默认表 [ExtendModelData]
 */
@Getter
@Setter
@TableName(CmsExtendModelData.TABLE_NAME)
public class CmsExtendModelData extends BaseModelData {
    
    public static final String TABLE_NAME = "cms_exd_default";

    /**
     * 关联数据ID（主键）
     */
    private Long dataId;

    /**
     * 关联数据类型（主键）
     */
    private String dataType;

    /**
     * 关联元数据模型ID
     */
    private Long modelId;
}
