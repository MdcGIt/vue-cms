package com.ruoyi.contentcore.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.dto.CatalogApplyChildrenDTO;
import com.ruoyi.contentcore.domain.dto.CatalogDTO;
import com.ruoyi.contentcore.domain.dto.SiteDefaultTemplateDTO;

public interface ICatalogService extends IService<CmsCatalog> {
	
	/**
	 * 根据栏目ID查找栏目数据
	 * 
	 * @param catalogId
	 * @return
	 */
	CmsCatalog getCatalog(Long catalogId);

	/**
	 * 根据栏目别名查找栏目数据
	 * 
	 * @param siteId
	 * @param catalogAlias
	 * @return
	 */
	CmsCatalog getCatalogByAlias(Long siteId, String catalogAlias);

	/**
	 * 构建栏目树数据
	 * 
	 * @param catalogs
	 * @return
	 */
	List<TreeNode<String>> buildCatalogTreeData(List<CmsCatalog> catalogs);

	/**
	 * 校验栏目名称、别名、目录是否重复
	 *
	 * @param siteId
	 * @param catalogId
	 * @param name
	 * @param alias
	 * @param path
	 * @return
	 */
	boolean checkCatalogUnique(Long siteId, Long catalogId, String name, String alias, String path);

    /**
     * 添加栏目
     * 
     * @param dto
     * @throws IOException 
     */
	public CmsCatalog addCatalog(CatalogDTO dto) throws IOException;

	/**
	 * 编辑栏目
	 * 
	 * @param dto
	 * @return
	 * @throws IOException 
	 */
	public CmsCatalog editCatalog(CatalogDTO dto) throws IOException;
	
	/**
	 * 删除栏目
	 * 
	 * @param catalogId
	 * @return
	 */
	public CmsCatalog deleteCatalog(long catalogId);

	/**
	 * 获取栏目链接
	 * 
	 * @param catalog
	 * @param publishPipeCode
	 * @param isPreview
	 * @return
	 */
	String getCatalogLink(CmsCatalog catalog, int pageIndex, String publishPipeCode, boolean isPreview);

	/**
	 * 站点配置应用到指定栏目或子栏目
	 * 
	 * @param dto
	 * @return
	 */
	void applyChildren(CatalogApplyChildrenDTO dto);
	
	/**
	 * 应用站点指定默认模板配置到指定的栏目
	 * 
	 * @param dto
	 * @return
	 */
	public void applySiteDefaultTemplateToCatalog(SiteDefaultTemplateDTO dto);

	/**
	 * 清理栏目缓存
	 * 
	 * @param catalog
	 */
	void clearCache(CmsCatalog catalog);

	/**
	 * 改变栏目显示状态
	 * 
	 * @param catalogId
	 * @param visible
	 */
	void changeVisible(Long catalogId, String visible);

	/**
	 * 转移栏目
	 * 
	 * @param fromCatalog
	 * @param toCatalog
	 * @return 
	 */
	AsyncTask moveCatalog(CmsCatalog fromCatalog, CmsCatalog toCatalog);
	
	/**
	 * 配置静态化模板上下文中的静态化文件路径
	 * 
	 * @param catalog
	 * @param context
	 * @param hasIndex
	 */
	void setStaticPath(CmsCatalog catalog, TemplateContext context, boolean hasIndex);

	/**
	 * 保存栏目扩展配置
	 * 
	 * @param catalogId
	 * @param configs
	 * @param operator
	 */
	void saveCatalogExtends(Long catalogId, Map<String, String> configs, String operator);
	
	/**
	 * 栏目排序
	 * 
	 * @param catalogId
	 * @param sort
	 */
	void sortCatalog(Long catalogId, Integer sort);
}
