package com.ruoyi.contentcore.perms.aspectj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.dev33.satoken.annotation.SaMode;

/**
 * 站点/栏目/内容权限校验
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface CmsPriv {
	
	/**
	 * 权限类型
	 */
	String type();

    /**
     * 需要校验的权限码
     */
    String[] value() default {};

    /**
     * 验证模式：AND | OR，默认OR
     */
    SaMode mode() default SaMode.OR;
}
