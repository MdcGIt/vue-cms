package com.ruoyi.common.i18n;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.AbstractApplicationContext;

import com.ruoyi.common.utils.ReflectASMUtils;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;

public class I18nUtils {

	private final static Pattern PATTERN = Pattern.compile("\\{([^}]*)\\}",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

	private final static Pattern LANG_PATTERN = Pattern.compile("\\$t\\{([^}]+)\\}",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

	private final static MessageSource messageSource = SpringUtils
			.getBean(AbstractApplicationContext.MESSAGE_SOURCE_BEAN_NAME);

	public static void replaceI18nFields(List<?> objs) {
		replaceI18nFields(objs, LocaleContextHolder.getLocale());
	}

	/**
	 * 将列表所有对象中@I18nField标注的字段替换为指定国际化语言值
	 * 
	 * @param objs
	 * @param locale
	 */
	public static void replaceI18nFields(List<?> objs, Locale locale) {
		if (Objects.isNull(objs)) {
			return;
		}
		objs.forEach(obj -> replaceI18nFields(obj, locale));
	}

	public static void replaceI18nFields(Object obj) {
		replaceI18nFields(obj, LocaleContextHolder.getLocale());
	}

	/**
	 * 将对象有@I18nField标注的字段替换为指定国际化语言值
	 * 
	 * @param obj
	 * @param locale
	 */
	public static void replaceI18nFields(Object obj, Locale locale) {
		if (Objects.isNull(obj)) {
			return;
		}
		Field[] declaredFields = obj.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			if (field.isAnnotationPresent(I18nField.class)) {
				I18nField i18nField = field.getAnnotation(I18nField.class);
				try {
					if (StringUtils.isNotEmpty(i18nField.value())) {
						String langKey = i18nField.value();
						langKey = replaceHolder(obj, langKey);
						String langValue = get(langKey, locale);
						if (!langKey.equals(langValue)) {
							ReflectASMUtils.invokeSetter(obj, field.getName(), langValue);
						}
					} else {
						Object fieldV = ReflectASMUtils.invokeGetter(obj, field.getName());
						if (Objects.nonNull(fieldV)) {
							String langKey = fieldV.toString();
							String langValue = get(langKey, locale);
							if (!langKey.equals(langValue)) {
								ReflectASMUtils.invokeSetter(obj, field.getName(), langValue);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 替换占位符
	 * 
	 * @param obj
	 * @param langKey
	 * @return
	 */
	private static String replaceHolder(Object obj, String langKey) {
		Matcher matcher = PATTERN.matcher(langKey);
		int lastIndex = 0;
		StringBuilder sb = new StringBuilder();

		while (matcher.find(lastIndex)) {
			int s = matcher.start();
			sb.append(langKey.substring(lastIndex, s));
			try {
				String fieldName = matcher.group(1);
				if (StringUtils.isNotEmpty(fieldName)) {
					Object fieldV = ReflectASMUtils.invokeGetter(obj, fieldName);
					if (Objects.nonNull(fieldV)) {
						sb.append(fieldV.toString());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			lastIndex = matcher.end();
		}
		sb.append(langKey.substring(lastIndex));
		return sb.toString();
	}

	/**
	 * 解析字符，将字符串中$t{}替换为当前默认国际化语言值
	 * 
	 * @param string
	 * @return
	 */
	public static String parse(String string) {
		return parse(string, LocaleContextHolder.getLocale());
	}

	/**
	 * 解析字符，将字符串中$t{}替换为对应国际化语言值
	 * 
	 * @param string
	 * @return
	 */
	public static String parse(String string, Locale locale, Object... args) {
		if (StringUtils.isEmpty(string)) {
			return string;
		}
		Matcher matcher = LANG_PATTERN.matcher(string);
		int lastIndex = 0;
		StringBuilder sb = new StringBuilder();
		while (matcher.find(lastIndex)) {
			int s = matcher.start();
			sb.append(string.substring(lastIndex, s));

			String langKey = matcher.group(1);
			sb.append(get(langKey, locale, args));

			lastIndex = matcher.end();
		}
		sb.append(string.substring(lastIndex));
		return sb.toString();
	}

	/**
	 * 获取国际化键名对应的当前默认语言值
	 * 
	 * @param langKey
	 * @return
	 */
	public static String get(String langKey) {
		return get(langKey, LocaleContextHolder.getLocale());
	}

	/**
	 * 获取国际化键名指定的语言值
	 * 
	 * @param langKey
	 * @param locale
	 * @param args
	 * @return
	 */
	public static String get(String langKey, Locale locale, Object... args) {
		if (StringUtils.isEmpty(langKey)) {
			return langKey;
		}
		return messageSource.getMessage(langKey, args, locale);
	}
}
