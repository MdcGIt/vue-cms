package com.ruoyi.system.groovy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;

import groovy.lang.GroovyClassLoader;
import jakarta.annotation.Resource;

public class GroovyScriptFactory {

	private static Logger logger = LoggerFactory.getLogger(GroovyScriptFactory.class);

	private static GroovyScriptFactory glueFactory = new GroovyScriptFactory();

	public static GroovyScriptFactory getInstance() {
		return glueFactory;
	}

	/**
	 * groovy class loader
	 */
	private GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
	
	private ConcurrentMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

	/**
	 * load new instance, prototype
	 *
	 * @param scriptText
	 * @return
	 * @throws Exception
	 */
	public BaseGroovyScript loadNewInstance(String scriptText) throws Exception {
		if (StringUtils.isNotBlank(scriptText)) {
			Class<?> clazz = getCodeSourceClass(scriptText);
			if (clazz != null) {
				Object instance = clazz.getDeclaredConstructor().newInstance();
				if (instance != null) {
					if (instance instanceof BaseGroovyScript groovyScript) {
						this.injectService(instance);
						return groovyScript;
					} else {
						throw new IllegalArgumentException(
								"Cannot convert from instance[" + instance.getClass() + "] to BaseGroovyScript");
					}
				}
			}
		}
		throw new IllegalArgumentException("Groovy script text cannot be empty.");
	}

	private Class<?> getCodeSourceClass(String codeSource) {
		try {
			// md5
			byte[] md5 = MessageDigest.getInstance("MD5").digest(codeSource.getBytes());
			String md5Str = new BigInteger(1, md5).toString(16);

			Class<?> clazz = CLASS_CACHE.get(md5Str);
			if (clazz == null) {
				clazz = groovyClassLoader.parseClass(codeSource);
				CLASS_CACHE.putIfAbsent(md5Str, clazz);
			}
			return clazz;
		} catch (Exception e) {
			return groovyClassLoader.parseClass(codeSource);
		}
	}

	/**
	 * inject action of spring
	 * 
	 * @param instance
	 */
	private void injectService(Object instance) {
		if (instance == null) {
			return;
		}
		Field[] fields = instance.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			Object fieldBean = null;
			// with bean-id, bean could be found by both @Resource and @Autowired, or bean
			// could only be found by @Autowired
			if (AnnotationUtils.getAnnotation(field, Resource.class) != null) {
				Resource resource = AnnotationUtils.getAnnotation(field, Resource.class);
				if (StringUtils.isNotBlank(resource.name())) {
					fieldBean = SpringUtils.getBean(resource.name());
				} else {
					fieldBean = SpringUtils.getBean(field.getName());
				}
				if (fieldBean == null) {
					fieldBean = SpringUtils.getBean(field.getType());
				}
			} else if (AnnotationUtils.getAnnotation(field, Autowired.class) != null) {
				Qualifier qualifier = AnnotationUtils.getAnnotation(field, Qualifier.class);
				if (Objects.nonNull(qualifier) && StringUtils.isNotBlank(qualifier.value())) {
					fieldBean = SpringUtils.getBean(qualifier.value());
				} else {
					fieldBean = SpringUtils.getBean(field.getType());
				}
			}

			if (fieldBean != null) {
				field.setAccessible(true);
				try {
					field.set(instance, fieldBean);
				} catch (IllegalArgumentException e) {
					logger.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}
}