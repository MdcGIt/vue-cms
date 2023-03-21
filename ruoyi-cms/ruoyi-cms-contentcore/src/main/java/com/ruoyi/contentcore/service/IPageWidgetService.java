package com.ruoyi.contentcore.service;

import java.io.IOException;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.contentcore.core.IPageWidget;
import com.ruoyi.contentcore.core.IPageWidgetType;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsPageWidget;

import freemarker.template.TemplateException;

public interface IPageWidgetService extends IService<CmsPageWidget> {

	/**
	 * 获取指定页面部件类型
	 * 
	 * @param type
	 * @return
	 */
	IPageWidgetType getPageWidgetType(String type);

	/**
	 * 获取页面部件类型列表
	 * 
	 * @return
	 */
	List<IPageWidgetType> getPageWidgetTypes();
	
	/**
	 * 添加页面部件数据
	 * 
	 * @param pw
	 * @return
	 * @throws IOException
	 */
	public void addPageWidget(IPageWidget pw);
	
	/**
	 * 修改页面部件数据
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	public void savePageWidget(IPageWidget pw);
	
	/**
	 * 删除页面部件数据
	 * 
	 * @param pageWidgetIds
	 * @return
	 * @throws IOException
	 */
	public void deletePageWidgets(List<Long> pageWidgetIds);

	/**
	 * 删除栏目相关页面部件数据
	 * 
	 * @param catalog
	 */
	void deletePageWidgetsByCatalog(CmsCatalog catalog);

	/**
	 * 发布页面部件
	 * 
	 * @param pageWidgetId
	 * @return
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	public void publishPageWidgets(List<Long> pageWidgetId) throws TemplateException, IOException;

	/**
	 * 是否存在重复编码
	 * 
	 * @param siteId
	 * @param code
	 * @param pageWidgetId
	 * @return
	 */
	boolean checkCodeUnique(Long siteId, String code, Long pageWidgetId);
}
