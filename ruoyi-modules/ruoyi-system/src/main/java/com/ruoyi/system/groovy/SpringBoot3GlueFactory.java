package com.ruoyi.system.groovy;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.glue.GlueFactory;
import com.xxl.job.core.handler.IJobHandler;

import groovy.lang.GroovyClassLoader;
import jakarta.annotation.Resource;

public class SpringBoot3GlueFactory extends GlueFactory {
	
	private static Logger logger = LoggerFactory.getLogger(SpringBoot3GlueFactory.class);

	private static GlueFactory glueFactory = new SpringBoot3GlueFactory();
	
	public static GlueFactory getInstance(){
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
	 * @param codeSource
	 * @return
	 * @throws Exception
	 */
	public IJobHandler loadNewInstance(String codeSource) throws Exception{
		if (codeSource!=null && codeSource.trim().length()>0) {
			Class<?> clazz = getCodeSourceClass(codeSource);
			if (clazz != null) {
				Object instance = clazz.getDeclaredConstructor().newInstance();
				if (instance!=null) {
					if (instance instanceof IJobHandler) {
						this.injectService(instance);
						return (IJobHandler) instance;
					} else {
						throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, "
								+ "cannot convert from instance["+ instance.getClass() +"] to IJobHandler");
					}
				}
			}
		}
		throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, instance is null");
	}
	private Class<?> getCodeSourceClass(String codeSource){
		try {
			// md5
			byte[] md5 = MessageDigest.getInstance("MD5").digest(codeSource.getBytes());
			String md5Str = new BigInteger(1, md5).toString(16);

			Class<?> clazz = CLASS_CACHE.get(md5Str);
			if(clazz == null){
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
	@Override
	public void injectService(Object instance) {
		if (instance == null) {
			return;
		}

		if (XxlJobSpringExecutor.getApplicationContext() == null) {
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
				try {
					Resource resource = AnnotationUtils.getAnnotation(field, Resource.class);
					if (resource.name() != null && resource.name().length() > 0) {
						fieldBean = XxlJobSpringExecutor.getApplicationContext().getBean(resource.name());
					} else {
						fieldBean = XxlJobSpringExecutor.getApplicationContext().getBean(field.getName());
					}
				} catch (Exception e) {
				}
				if (fieldBean == null) {
					fieldBean = XxlJobSpringExecutor.getApplicationContext().getBean(field.getType());
				}
			} else if (AnnotationUtils.getAnnotation(field, Autowired.class) != null) {
				Qualifier qualifier = AnnotationUtils.getAnnotation(field, Qualifier.class);
				if (qualifier != null && qualifier.value() != null && qualifier.value().length() > 0) {
					fieldBean = XxlJobSpringExecutor.getApplicationContext().getBean(qualifier.value());
				} else {
					fieldBean = XxlJobSpringExecutor.getApplicationContext().getBean(field.getType());
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