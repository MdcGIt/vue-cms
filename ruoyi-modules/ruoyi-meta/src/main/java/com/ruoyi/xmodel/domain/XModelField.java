package com.ruoyi.xmodel.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;
import com.ruoyi.xmodel.dto.FieldOptions;

import lombok.Getter;
import lombok.Setter;


/**
 * 元数据模型字段定义表[XModelField]
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Getter
@Setter
@TableName(value = XModelField.TABLE_NAME, autoResultMap = true)
public class XModelField extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "x_model_field";

    @TableId(value = "field_id", type = IdType.INPUT)
    private Long fieldId;

    /**
     * 模型ID
     */
    private Long modelId;

    /**
     * 名称
     */
    private String name;

    /**
     * 唯一标识编码
     */
    private String code;

    /**
     * 如果是x_model_data表，字段类型：varchar(50)，varchar(200)，varchar(2000)，mediumText，bigint，double，datetime
     */
    private String fieldType;
    
    /**
     * 如果是自定义表，对应数据库表字段名
     */
    private String fieldName;

    /**
     * 对应前端显示控件类型
     */
    private String controlType;
    
    /**
     * 是否必填
     */
    private String mandatoryFlag;
    
    /**
     * 可选项
     * 格式：
     * 	{ 'type':'dict','value': "dict_type"}<br/>
     * { 'type':'text','value': "v1=label1\nv2=label2\n..."}<br/>
     * type: <br/>
     * 	 dict = 数据字典，例如：cms_content_status<br/>
     *   text = 自定义格式：<名称=值>\n...
     *   	
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private FieldOptions options;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 字段值
     */
    @TableField(exist = false)
    private String value;
}
