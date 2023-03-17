package com.ruoyi.contentcore.core;

import org.apache.commons.lang3.ArrayUtils;

import com.ruoyi.common.utils.StringUtils;

/**
 * 扩展属性定义接口
 */
public interface IProperty {
	
	/**
	 * Bean名称前缀
	 */
	public static final String BEAN_NAME_PREFIX = "CMSProperty_";
	
	/**
	 * 属性适用范围定义：站点、栏目
	 *
	 */
	public enum UseType {
		Site, Catalog
	}

	/**
	 * 属性适用范围
	 * @return
	 */
	public UseType[] getUseTypes();

	/**
	 * 属性值ID
	 */
	public String getId();
	
	/**
	 * 属性值名称
	 */
	public String getName();
	
	/**
	 * 校验使用类型
	 */
	default public boolean checkUseType(UseType useType) {
		return ArrayUtils.contains(this.getUseTypes(), useType);
	}
	
	/**
	 * 属性值校验
	 */
	default public boolean validate(String value) {
		return true;
	}
	
	/**
	 * 属性默认值
	 */
	default public String defaultValue() {
		return StringUtils.EMPTY;
	}

	/**
	 * 属性值如果是复杂对象，需定义对象类型
	 */
	default public Class<?> valueClass() {
		return null;
	}
}
