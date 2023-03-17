package com.ruoyi.contentcore.core;

import java.io.IOException;

import com.ruoyi.common.utils.StringUtils;

import freemarker.template.TemplateException;

/**
 * 内部数据类型
 */
public interface IInternalDataType {
	
	/**
	 * Bean名称前缀
	 */
	public static final String BEAN_NAME_PREFIX = "InternalDataType_";
	
	/**
	 * 获取内部数据预览地址
	 * 
	 * 预览路径规则：cms/preview/{内部数据类型}/{数据id}?pp={发布通道编码}&pi={页码}
	 * 
	 * @param type
	 * @param id
	 * @param publishPipeCode
	 * @param pageIndex
	 * @return
	 */
	public static String getPreviewPath(String type, Long id, String publishPipeCode, int pageIndex) {
		String path = "cms/preview/" + type + "/" + id + "?pp=" + publishPipeCode;
		if (pageIndex > 1) {
			path += "&pi=" + pageIndex;
		}
		return path;
	}

	public static String getPreviewPath(String type, Long id, String publishPipeCode) {
		return getPreviewPath(type, id, publishPipeCode, 1);
	}

	/**
	 * 获取内部数据动态浏览地址
	 * 
	 * 动态路径规则：cms/view/{内部数据类型}/{数据id}?pp={发布通道编码}&pi={页码}
	 * 
	 * @param type
	 * @param id
	 * @param publishPipeCode
	 * @param pageIndex
	 * @return
	 */
	public static String getViewPath(String type, Long id, String publishPipeCode, int pageIndex) {
		String path = "cms/preview/" + type + "/" + id + "?pp=" + publishPipeCode;
		if (pageIndex > 1) {
			path += "&pi=" + pageIndex;
		}
		return path;
	}

	public static String getViewPath(String type, Long id, String publishPipeCode) {
		return getViewPath(type, id, publishPipeCode, 1);
	}
	
	/**
	 * 类型ID
	 */
	public String getId();

	/**
	 * 获取模板解析页面内容
	 * 
	 * @param dataId
	 * @param pageIndex
	 * @param publishPipeCode
	 * @param preview
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	default public String getPageData(Long dataId, int pageIndex, String publishPipeCode, boolean preview) throws IOException, TemplateException {
		return StringUtils.EMPTY;
	}
	
	/**
	 * 访问链接
	 * 
	 * @param dataId
	 * @param pageIndex
	 * @param publishPipeCode
	 * @param preview
	 * @return
	 */
	default public String getLink(InternalURL internalUrl, int pageIndex, String publishPipeCode, boolean preview) {
		return StringUtils.EMPTY;
	}
}
