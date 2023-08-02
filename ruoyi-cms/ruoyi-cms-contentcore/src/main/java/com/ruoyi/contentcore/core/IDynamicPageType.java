package com.ruoyi.contentcore.core;

import com.ruoyi.common.staticize.core.TemplateContext;

import java.util.Map;

/**
 * 动态页面类型
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IDynamicPageType {

    String BEAN_PREFIX = "DynamicPageType_";

    /**
     * 类型
     */
    String getType();

    /**
     * 名称
     */
    String getName();

    /**
     * 访问路径
     */
    String getRequestPath();

    /**
     * 模板发布通道配置属性KEY
     */
    String getPublishPipeKey();

    /**
     * 校验请求参数
     *
     * @param parameters
     */
    default void validate(Map<String, String> parameters) {

    }

    /**
     * 模板初始化数据
     *
     * @param parameters
     * @param templateContext
     */
    default void initTemplateData(Map<String, String> parameters, TemplateContext templateContext) {

    }
}
