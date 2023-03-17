package com.ruoyi.contentcore.properties;

import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.JacksonUtils;
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
	
	public static FileStorageArgs getFileStorageArgs(Map<String, Object> props) {
		String stringValue = ConfigPropertyUtils.getStringValue(ID, props);
		FileStorageArgs args = JacksonUtils.from(stringValue, FileStorageArgs.class);
		if (Objects.isNull(args)) {
			args = new FileStorageArgs();
		}
		return args;
	}

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
	public String defaultValue() {
		return JacksonUtils.to(new FileStorageArgs());
	}

	@Override
	public Class<?> valueClass() {
		return FileStorageArgs.class;
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
