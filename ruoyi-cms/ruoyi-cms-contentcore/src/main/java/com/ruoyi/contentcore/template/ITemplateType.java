package com.ruoyi.contentcore.template;

import com.ruoyi.common.staticize.core.TemplateContext;

public interface ITemplateType {
	
	/**
	 * Bean名称前缀
	 */
	public static final String BEAN_NAME_PREFIX = "TemplateType_";

	/**
	 * 模板类型唯一标识
	 */
	public String getId();

	/**
	 * 初始化当前模板类型的上下文变量
	 * 
	 * @param dataId
	 * @param context
	 */
	default public void initTemplateData(Object dataId, TemplateContext context) {

	}
}
