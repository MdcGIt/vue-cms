package com.ruoyi.contentcore.core;

import java.util.List;

import com.ruoyi.common.utils.StringUtils;

/**
 * 发布通道属性
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IPublishPipeProp {
	
	String DetailTemplatePropPrefix = "detailTemplate_";
	
	String DefaultDetailTemplatePropPrefix = "defaultDetailTemplate_";

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
