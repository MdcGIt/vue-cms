package com.ruoyi.contentcore.core;

import java.io.IOException;

import org.apache.commons.lang3.ArrayUtils;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsResource;

public interface IResourceType {
	
	/**
	 * Bean名称前缀
	 */
	public static final String BEAN_NAME_PREFIX = "ResourceType_";
    
    /**
     * 站点上传资源文件目录
     */
    public final static String UploadResourceDirectory = "resources/";

	String getId();

	/**
	 * 资源类型所用后缀
	 */
	String[] getUsableSuffix();
	
	/**
	 * 校验文件后缀是否符合当前资源类型
	 * 
	 * @param fileSuffix
	 * @return
	 */
	default public boolean check(String suffix) {
		return ArrayUtils.contains(this.getUsableSuffix(), suffix.toLowerCase());
	}
	
	default public String getUploadPath() {
		return UploadResourceDirectory + this.getId() + StringUtils.SLASH;
	}
	
	/**
	 * 处理资源：提取资源属性、添加水印等
	 * 
	 * @param resource
	 * @throws IOException 
	 */
	default public byte[] process(CmsResource resource, byte[] bytes) throws IOException {
		resource.setFileSize((long) bytes.length);
		return bytes;
	}
}
