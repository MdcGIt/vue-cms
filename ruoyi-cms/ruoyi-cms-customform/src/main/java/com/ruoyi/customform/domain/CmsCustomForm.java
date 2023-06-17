package com.ruoyi.customform.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义表单对象 [CmsCustomForm]
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@TableName(value = CmsCustomForm.TABLE_NAME, autoResultMap = true)
public class CmsCustomForm extends BaseEntity {

    private static final long serialVersionUID=1L;
    
    public static final String TABLE_NAME = "cms_custom_form";

    @TableId(value = "form_id", type = IdType.INPUT)
    private Long formId;
    
    /**
     * 所属站点ID
     */
    private Long siteId;
    
    /**
     * 关联元数据模型ID
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
     * 状态（草稿，发布，下线）
     */
    private Integer status;

    /**
     * 是否需要验证码
     */
    private String needCaptcha;

    /**
     * 是否需要会员登录
     */
    private String needLogin;

    /**
     * 提交用户唯一性限制（IP、浏览器指纹）
     */
    private String ruleLimit;

    /**
     * 发布通道模板配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> templates;

    public Map<String, String> getTemplates() {
        if (this.templates == null) {
            this.templates = new HashMap<>();
        }
        return this.templates;
    }
}
