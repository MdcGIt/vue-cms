package com.ruoyi.common.mybatisplus.db;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DBTableColumn {

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段类型
     */
    private String type;

    /**
     * 字段备注
     */
    private String columnComment;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否自增
     */
    private Boolean autoIncrement;

    /**
     * 是否主键
     */
    private boolean primary;

    /**
     * 是否可为空
     */
    private boolean nullable;

    public DBTableColumn(String name, String type, String defaultValue, Boolean nullable,
             Boolean primary, Boolean autoIncrement) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.nullable = nullable;
        this.primary = primary;
        this.autoIncrement = autoIncrement;
    }
}
