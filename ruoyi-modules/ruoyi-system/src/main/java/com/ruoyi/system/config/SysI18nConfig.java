package com.ruoyi.system.config;

import java.nio.charset.Charset;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysI18nDictService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * 动态国际化字符串存储数据库
 */
@Configuration
@RequiredArgsConstructor
public class SysI18nConfig {
	
	@Value("${spring.messages.basename:i18n/messages}")
	private String basename;
	
	@Value("${spring.messages.cache-seconds:7200}")
	private int cacheSeconds;
	
	@Value("${spring.messages.encoding:UTF-8}")
	private String encoding;

	private final RedisCache redisCache;
	
	private final ISysI18nDictService i18nDictService;
	
	@Bean("messageSource")
	@DependsOn(value = { "flywayInitializer" })
	public MessageSource messageSource() {
		I18nMessageSource messageSource = new I18nMessageSource(this.redisCache, this.i18nDictService);
		messageSource.setBasename(this.basename);
		messageSource.setCacheSeconds(this.cacheSeconds);
		messageSource.setEncoding(Charset.forName(this.encoding));
		messageSource.setUseCodeAsDefaultMessage(true);
		return messageSource;
	}

	/**
	 * 语言区域解析器
	 * 
	 * RequestParam -> Accept-Language -> Default
	 * 
	 * @return
	 */
	@Bean
	public LocaleResolver localeResolver() {
		XyLocaleResolver localeResolver = new XyLocaleResolver();
		localeResolver.setDefaultLocale(Locale.getDefault());
		return localeResolver;
	}

	public static class XyLocaleResolver extends AcceptHeaderLocaleResolver {
		
		@Override
		public Locale resolveLocale(HttpServletRequest request) {
			String languageTag = request.getParameter("lang");
			if (StringUtils.isNotEmpty(languageTag)) {
				Locale locale = Locale.forLanguageTag(languageTag);
				if (this.getSupportedLocales().contains(locale)) {
					return locale;
				}
			}
			return super.resolveLocale(request);
		}
	}
}
