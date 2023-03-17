package com.ruoyi.contentcore.service;

import java.io.IOException;
import java.util.List;

import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.core.IPageWidget;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.domain.CmsSite;

import freemarker.template.TemplateException;

public interface IPublishService {
	
	/**
	 * 发布站点首页<br/>
	 * 
	 * 此方法供API发布站点首页调用，做基础校验
	 * 
	 * @param site
	 * @return
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	void publishSiteIndex(CmsSite site) throws IOException, TemplateException;

	/**
	 * 发布全站，异步任务
	 * 
	 * 发布站点下所有栏目及指定状态内容
	 * 
	 * @param site
	 * @param contentStatus
	 * @return
	 */
	AsyncTask publishAll(CmsSite site, String contentStatus);

	/**
	 * 站点首页静态化<br/>
	 * 
	 * 此方法仅做静态化逻辑，提供发布内容/栏目调用支持，支持异步任务
	 * 
	 * @param site
	 * @param task
	 */
	void siteStaticize(CmsSite site);

	/**
	 * 站点首页页面内容
	 * 
	 * @param site
	 * @param publishPipeCode
	 * @return
	 * @throws IOException
	 * @throws TemplateException 
	 */
	String getSitePageData(CmsSite site, String publishPipeCode, boolean isPreview)
			throws IOException, TemplateException;

	/**
	 * 获取栏目模板页面内容
	 * 
	 * @param catalog
	 * @param publishPipeCode
	 * @param writer
	 * @param preview
	 * @return
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	String getCatalogPageData(CmsCatalog catalog, int pageIndex, String publishPipeCode, boolean isPreview)
			throws IOException, TemplateException;

	/**
	 * 发布栏目<br/>
	 * 同步方法，仅生成指定栏目各个发布通的静态文件
	 * 
	 * @param catalog
	 * @param pageMax 
	 * @param task 
	 */
	void catalogStaticize(CmsCatalog catalog, int pageMax);
	
	/**
	 * 发布栏目，异步任务
	 * 
	 * @param catalog
	 * @param publishChild 是否发布子栏目 
	 * @param publishDetail 是否发布详情页
	 * @param publishStatus 指定发布内容状态
	 * @return
	 */
	AsyncTask publishCatalog(CmsCatalog catalog, boolean publishChild, boolean publishDetail, String publishStatus);

	/**
	 * 获取内容模板页面结果
	 * 
	 * @param content
	 * @param pageIndex
	 * @param publishPipeCode
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	String getContentPageData(CmsContent content, int pageIndex, String publishPipeCode, boolean isPreview)
			throws IOException, TemplateException;
	
	/**
	 * 发布内容
	 * 
	 * @param contentIds
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	void publishContent(List<Long> contentIds) throws IOException, TemplateException;

	/**
	 * 内容静态化<br/>
	 * 
	 * 内容发布逻辑执行调用，此方法供内容触发发布调用。<br/>
	 * 创建异步任务关联静态化内容相关的映射内容、级联栏目及站点页面。
	 * 
	 * @param content
	 * @param task
	 */
	void contentStaticize(IContent<?> content);
	
	/**
	 * 内容异步静态化<br/>
	 * 供栏目发布及站点发布调用
	 * 
	 * @param cmsContent
	 * @param task
	 */
	void contentStaticize(CmsContent cmsContent);

	/**
	 * 获取页面部件模板解析内容
	 * 
	 * @param pageWidget
	 * @param isPreview
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	public String getPageWidgetPageData(CmsPageWidget pageWidget, boolean isPreview) throws IOException, TemplateException;
	
	/**
	 * 页面部件静态化
	 * 
	 * @param pageWidget
	 * @throws TemplateException
	 * @throws IOException
	 */
	public void pageWidgetStaticize(IPageWidget pageWidget) throws TemplateException, IOException;
}
