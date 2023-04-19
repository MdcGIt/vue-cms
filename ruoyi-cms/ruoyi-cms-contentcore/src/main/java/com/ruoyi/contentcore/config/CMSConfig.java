package com.ruoyi.contentcore.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.ContentCoreConsts;
import com.ruoyi.contentcore.config.properties.CMSProperties;
import com.ruoyi.contentcore.fixed.config.BackendContext;

import freemarker.cache.FileTemplateLoader;

@Configuration
@EnableConfigurationProperties(CMSProperties.class)
public class CMSConfig implements WebMvcConfigurer {

	private static String CACHE_PREFIX;

	private static String RESOURCE_ROOT;
	
	public CMSConfig(CMSProperties properties) throws FileNotFoundException {
		// CMS缓存前缀
		CACHE_PREFIX = properties.getCacheName();
		// 站点资源存放根目录
		RESOURCE_ROOT = properties.getResourceRoot();
		if (StringUtils.isEmpty(RESOURCE_ROOT)) {
			RESOURCE_ROOT = SpringUtils.getAppParentDirectory() + "/wwwroot_release/";
		}
		RESOURCE_ROOT = FileExUtils.normalizePath(RESOURCE_ROOT);
		if (!RESOURCE_ROOT.endsWith("/")) {
			RESOURCE_ROOT += "/";
		}
		FileExUtils.mkdirs(RESOURCE_ROOT);
		properties.setResourceRoot(RESOURCE_ROOT);
	}
	
	@Bean
	public FileTemplateLoader cmsFileTemplateLoader() throws IOException {
		return new FileTemplateLoader(new File(RESOURCE_ROOT));
	}

	public String getCachePrefix() {
		return CACHE_PREFIX;
	}

	/**
	 * 获取本地资源存储根目录
	 * <p>
	 * 如果未配置，开发环境取项目根目录下wwwroot_release，部署环境取项目部署同级目录wwwroot_release
	 * </p>
	 */
	public static String getResourceRoot() {
		return RESOURCE_ROOT;
	}

	public static String getResourcePreviewPrefix() {
		return BackendContext.getValue() + ContentCoreConsts.RESOURCE_PREVIEW_PREFIX;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		/** 本地文件上传路径 */
		registry.addResourceHandler(ContentCoreConsts.RESOURCE_PREVIEW_PREFIX + "**")
				.addResourceLocations("file:" + getResourceRoot());
	}
}