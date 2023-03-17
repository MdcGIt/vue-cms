package com.ruoyi.system.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.ConvertUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysI18nDict;
import com.ruoyi.system.service.ISysI18nDictService;

import lombok.RequiredArgsConstructor;

@Getter
@Setter
@RequiredArgsConstructor
public class I18nMessageSource extends AbstractMessageSource implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(I18nMessageSource.class);

	private final static String CACHE_PREFIX = "i18n:";

	private final RedisCache redisCache;

	private final ISysI18nDictService i18nDictService;

	private String basename;

	private Charset encoding = StandardCharsets.UTF_8;

	private int cacheSeconds;

	private Locale defaultLocale = Locale.SIMPLIFIED_CHINESE;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		this.loadMessages();
	}

	@Override
	protected MessageFormat resolveCode(String code, Locale locale) {
		Object value = this.redisCache.getCacheMapValue(CACHE_PREFIX + locale.toLanguageTag(), code);
		if (Objects.nonNull(value)) {
			return new MessageFormat(value.toString(), locale);
		}
		return null;
	}
	
	@Override
	protected String resolveCodeWithoutArguments(String code, Locale locale) {
		Object value = this.redisCache.getCacheMapValue(CACHE_PREFIX + locale.toLanguageTag(), code);
		return ConvertUtils.toStr(value);
	}

	@Override
	protected String getDefaultMessage(String code) {
		Object value = this.redisCache.getCacheMapValue(CACHE_PREFIX + this.getDefaultLocale().toLanguageTag(), code);
		return ConvertUtils.toStr(value, isUseCodeAsDefaultMessage() ? code : StringUtils.EMPTY);
	}

	private void loadMessages() throws IOException {
		long s = System.currentTimeMillis();
		// 读取配置文件数据
		this.loadMessagesFromResources();
		// 加载数据库数据，如果与配置文件重复则直接覆盖掉
		this.loadMessagesFromDB();

		logger.debug("Load i18n messages cost: {}ms", System.currentTimeMillis() - s);
	}

	private void loadMessagesFromDB() {
		Map<String, Map<String, String>> map = i18nDictService.list().stream().collect(Collectors.groupingBy(
				SysI18nDict::getLangTag, Collectors.toMap(SysI18nDict::getLangKey, SysI18nDict::getLangValue)));
		map.entrySet().forEach(e -> {
			e.getValue().entrySet().forEach(kv -> {
				redisCache.setCacheMapValue(CACHE_PREFIX + e.getKey(), kv.getKey(), kv.getValue());
			});
		});
	}

	private void loadMessagesFromResources() throws IOException {
		if (StringUtils.isEmpty(this.getBasename())) {
			return;
		}
		String target = this.basename.replace('.', '/');
		Resource[] resources = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader())
				.getResources("classpath*:" + target + "*.properties");
		for (Resource resource : resources) {
			String langTag = this.getDefaultLocale().toLanguageTag();
			if (resource.getFilename().indexOf("_") > 0) {
				langTag = resource.getFilename().substring(resource.getFilename().indexOf("_") + 1,
						resource.getFilename().lastIndexOf("."));
				langTag = StringUtils.replace(langTag, "_", "-");
			}
			try (InputStream is = resource.getInputStream()) {
				List<String> lines = IOUtils.readLines(is, this.getEncoding());
				Map<String, String> map = lines.stream().map(s -> StringUtils.split(s, "="))
						.filter(a -> Objects.nonNull(a) && a.length == 2)
						.collect(Collectors.toMap(a -> a[0], a -> a[1]));
				redisCache.setCacheMap(CACHE_PREFIX + langTag, map);
			}
		}
	}
}
