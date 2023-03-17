package com.ruoyi.system.config;

import java.io.FileNotFoundException;
import java.util.Arrays;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ruoyi.common.security.intercepter.DemoModeIntercepter;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.system.SysConstants;
import com.ruoyi.system.config.properties.SysProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(SysProperties.class)
public class SystemConfig implements WebMvcConfigurer {
	
	/**
	 * 通用资源文件上传目录
	 */
	private static String UPLOAD_DIRECTORY;
	
	private final SysProperties properties;
	
	public SystemConfig(SysProperties properties) {
		UPLOAD_DIRECTORY = properties.getUploadPath();
		if (StringUtils.isEmpty(UPLOAD_DIRECTORY)) {
			try {
				UPLOAD_DIRECTORY = ResourceUtils.getURL("classpath:").getPath();
				if (UPLOAD_DIRECTORY.indexOf("/target/classes") > -1) {
					if (UPLOAD_DIRECTORY.endsWith(StringUtils.SLASH)) {
						UPLOAD_DIRECTORY = UPLOAD_DIRECTORY.substring(0, UPLOAD_DIRECTORY.length() - 1);
					}
					String[] arr = StringUtils.splitIgnoreEmpty(UPLOAD_DIRECTORY, StringUtils.SLASH);
					UPLOAD_DIRECTORY = StringUtils.join(Arrays.copyOfRange(arr, 0, arr.length - 4), StringUtils.SLASH);
				}
				UPLOAD_DIRECTORY += "/profile/";
				FileExUtils.mkdirs(UPLOAD_DIRECTORY);
				properties.setUploadPath(UPLOAD_DIRECTORY);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		if (!UPLOAD_DIRECTORY.endsWith("/")) {
			UPLOAD_DIRECTORY += "/";
		}
		UPLOAD_DIRECTORY = FileExUtils.normalizePath(UPLOAD_DIRECTORY);
		properties.setUploadPath(UPLOAD_DIRECTORY);
		log.info("System upload directory: " + UPLOAD_DIRECTORY);
		this.properties = properties;
	}
	
	/**
	 * 获取资源文件上传根目录
	 */
	public static String getUploadDir() {
		return UPLOAD_DIRECTORY;
	}
	
	/**
	 * 获取资源文件预览地址前缀
	 */
	public static String getResourcePrefix() {
		return SysConstants.RESOURCE_PREFIX;
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		/** 本地文件上传路径 */
		registry.addResourceHandler(getResourcePrefix() + "**")
				.addResourceLocations("file:" + this.properties.getUploadPath());
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 演示模式
		if (properties.isDemoMode()) {
			registry.addInterceptor(new DemoModeIntercepter()).addPathPatterns("/**").excludePathPatterns("/login",
					"/logout", "/captchaImage");
		}
	}
}
