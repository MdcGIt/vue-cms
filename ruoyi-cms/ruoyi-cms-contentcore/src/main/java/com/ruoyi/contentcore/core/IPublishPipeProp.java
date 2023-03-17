package com.ruoyi.contentcore.core;

import java.util.List;

import com.ruoyi.common.utils.StringUtils;

/**
 * 发不通道属性
 */
public interface IPublishPipeProp {
	
	public String DetailTemplatePropPrefix = "detailTemplate_";
	
	public String DefaultDetailTemplatePropPrefix = "defaultDetailTemplate_";

	/**
	 * 属性唯一标识键名
	 */
	public String getKey();
	
	/**
	 * 属性名称
	 */
	public String getName();
	
	/**
	 * 属性应用类型
	 */
	public List<PublishPipePropUseType> getUseTypes();
	
	/**
	 * 默认值
	 */
	default public String getDefaultValue() {
		return StringUtils.EMPTY;
	}
	
	/**
	 * 应用类型
	 */
	public enum PublishPipePropUseType {
		Site, Catalog, Content
	}
}
