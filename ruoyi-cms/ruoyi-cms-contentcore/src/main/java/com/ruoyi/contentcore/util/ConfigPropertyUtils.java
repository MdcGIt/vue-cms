package com.ruoyi.contentcore.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;

import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.ObjectUtils;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.core.IProperty.UseType;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;

public class ConfigPropertyUtils {
	
	private static final Map<String, IProperty> ConfigProperties = SpringUtils.getBeanMap(IProperty.class);
	
	/**
	 * 获取配置属性声明类
	 * 
	 * @param propertyKey
	 * @return
	 */
	public static IProperty getConfigProperty(String propertyKey) {
		return ConfigProperties.get(IProperty.BEAN_NAME_PREFIX + propertyKey);
	}

	/**
	 * 获取指定使用场景的配置属性声明类列表
	 * 
	 * @param useType
	 * @return
	 */
	public static List<IProperty> getConfigPropertiesByUseType(UseType useType) {
		return ConfigProperties.values().stream().filter(p -> p.checkUseType(useType)).collect(Collectors.toList());
	}
	
	/**
	 * 过滤掉map中不符合条件的key-value.
	 * 
	 * @param configProps
	 * @param useType
	 */
	public static void filterConfigProps(Map<String, Object> configProps, UseType useType) {
		for (Iterator<Entry<String, Object>> iterator = configProps.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Object> e = iterator.next();
			IProperty property = getConfigProperty(e.getKey());
			if (property == null || !property.checkUseType(useType)) {
				iterator.remove();
				continue;
			}
			if (!property.validate(e.getValue().toString())) {
				throw ContentCoreErrorCode.INVALID_PROPERTY.exception(property.getId(), e.getValue());
			}
			if (Objects.nonNull(property.valueClass())) {
				configProps.put(e.getKey(), JacksonUtils.to(e.getValue()));
			}
		}
	}
	
	/**
	 * 获取字符串类型配置属性值
	 * 
	 * @param propertyKey
	 * @param props
	 * @return
	 */
	public static String getStringValue(String propertyKey, Map<String, Object> props) {
		return getStringValue(propertyKey, props, null);
	}

	
	/**
	 * 获取字符串类型配置属性值，优先firstProps，firstProps没有则查找secondProps
	 * 
	 * @param propertyKey
	 * @param firstProps
	 * @param secondProps
	 * @return
	 */
	public static String getStringValue(String propertyKey, Map<String, Object> firstProps, Map<String, Object> secondProps) {
		IProperty prop = getConfigProperty(propertyKey);
		if (prop != null) {
			if (Objects.isNull(firstProps) && Objects.isNull(secondProps)) {
				return prop.defaultValue();
			}
			String v = MapUtils.getString(firstProps, prop.getId());
			if (StringUtils.isNotEmpty(v)) {
				return v;
			}
			return MapUtils.getString(secondProps, prop.getId(), prop.defaultValue());
		}
		return null;
	}

	/**
	 * 获取整数类型配置属性值
	 * 
	 * @param propertyKey
	 * @param props
	 * @return
	 */
	public static int getIntValue(String propertyKey, Map<String, Object> props) {
		return getIntValue(propertyKey, props, null);
	}

	/**
	 * 获取整数类型配置属性值，优先firstProps，firstProps没有则查找secondProps
	 * 
	 * @param propertyKey
	 * @param firstProps
	 * @param secondProps
	 * @return
	 */
	public static int getIntValue(String propertyKey, Map<String, Object> firstProps, Map<String, Object> secondProps) {
		IProperty prop = getConfigProperty(propertyKey);
		if (prop != null) {
			int defaultV = Integer.parseInt(prop.defaultValue());
			if (ObjectUtils.isAnyNull(firstProps, secondProps)) {
				return defaultV;
			}
			int v = MapUtils.getIntValue(firstProps, prop.getId());
			if (v != 0) {
				return v;
			}
			return MapUtils.getIntValue(secondProps, prop.getId(), defaultV);
		}
		return 0;
	}
}
