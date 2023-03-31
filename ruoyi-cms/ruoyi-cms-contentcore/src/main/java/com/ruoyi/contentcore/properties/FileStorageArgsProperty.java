package com.ruoyi.contentcore.properties;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 资源存储策略参数
 */
@Component(IProperty.BEAN_NAME_PREFIX + FileStorageArgsProperty.ID)
public class FileStorageArgsProperty implements IProperty {

	public final static String ID = "FileStorageArgs";
	
	static UseType[] UseTypes = new UseType[] { UseType.Site };

	@Override
	public UseType[] getUseTypes() {
		return UseTypes;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "资源存储策略参数";
	}
	
	@Override
	public FileStorageArgs defaultValue() {
		return new FileStorageArgs();
	}
	
	@Override
	public FileStorageArgs getPropValue(Map<String, String> configProps) {
		String v = MapUtils.getString(configProps, getId());
		if (StringUtils.isNotEmpty(v)) {
			return JacksonUtils.from(v, FileStorageArgs.class);
		}
		return defaultValue();
	}	
	
	public static FileStorageArgs getValue(Map<String, String> props) {
		String v = ConfigPropertyUtils.getStringValue(ID, props);
		if (StringUtils.isNotEmpty(v)) {
			return JacksonUtils.from(v, FileStorageArgs.class);
		}
		return new FileStorageArgs();
	}
	
	@Getter
	@Setter
	public static class FileStorageArgs {
		
		private String accessKey;
		
		private String accessSecret;
		
		private String endpoint;
		
		private String bucket;
		
		private String pipeline;
		
		private String domain;
	}
}
