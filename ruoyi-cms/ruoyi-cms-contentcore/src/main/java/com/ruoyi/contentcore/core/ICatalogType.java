package com.ruoyi.contentcore.core;

public interface ICatalogType {
	
	/**
	 * Bean名称前缀
	 */
	public static final String BEAN_NAME_PREFIX = "CatalogType_";

	/**
	 * 栏目类型ID
	 */
	public String getId();
	
	/**
	 * 栏目类型名称
	 */
	public String getName();
}
