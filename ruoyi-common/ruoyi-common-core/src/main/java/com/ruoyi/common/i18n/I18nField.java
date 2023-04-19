package com.ruoyi.common.i18n;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ruoyi.common.utils.StringUtils;

/**
 * 类字段添加此注释，根据国际化字典表获取当前语言环境翻译.@see I18nUtils.replaceI18nFields
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface I18nField {
	
	/**
	 * 国际化表键名，为空时使用字段值获取当前语言环境值
	 * <p>
	 * 可使用占位符来获取当前对象其他字段属性值，例如实体类的ID字段值，可使用#{fieldName}，大括号内是字段名，会被替换为指定字段的值。
	 * </p>
	 */
	public String value() default StringUtils.EMPTY;
}
