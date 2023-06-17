package com.ruoyi.common.mybatisplus.db;

import lombok.Getter;
import lombok.Setter;

/**
 * 字段值映射类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class FieldValue {

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段值
     */
    private String value;
}
