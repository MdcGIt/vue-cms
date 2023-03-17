package com.ruoyi.common.extend.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ruoyi.common.extend.ExtendConstants;
import com.ruoyi.common.extend.enums.LimitType;

/**
 * 限流注解
 * 
 * @author ruoyi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
	
	/**
	 * 限流缓存key前缀
	 */
	public String prefix() default ExtendConstants.RATE_LIMIT_KEY;

	/**
	 * 限流时间,单位秒
	 */
	public int expire() default 60;

	/**
	 * 限流阈值，单位时间内的请求上限
	 */
	public int limit() default 100;

	/**
	 * 限流类型
	 */
	public LimitType limitType() default LimitType.DEFAULT;
}
