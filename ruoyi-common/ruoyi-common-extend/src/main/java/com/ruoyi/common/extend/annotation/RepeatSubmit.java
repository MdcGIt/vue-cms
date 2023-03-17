package com.ruoyi.common.extend.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {
	
	/**
	 * 间隔时间(ms)，小于此时间视为重复提交，默认：2000ms
	 */
	public int interval() default 2000;
	
	/**
	 * 用户类型，使用指定用户token作为缓存键值组成部分，若无登录用户，则前端请求参数必须附带唯一标识，参数名：uuid
	 */
	public String userType() default "";
}
