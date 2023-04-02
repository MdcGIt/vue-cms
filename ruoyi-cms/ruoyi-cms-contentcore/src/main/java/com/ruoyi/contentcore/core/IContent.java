package com.ruoyi.contentcore.core;

import java.time.LocalDateTime;
import java.util.Map;

import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;

public interface IContent<T> {

	/**
	 * 获取站点ID
	 */
	public Long getSiteId();

	/**
	 * 获取所属站点
	 */
	public CmsSite getSite();

	/**
	 * 获取栏目ID
	 */
	public Long getCatalogId();

	/**
	 * 获取所属栏目
	 */
	public CmsCatalog getCatalog();

	/**
	 * 获取内容类型
	 */
	public String getContentType();

	/**
	 * 获取内容Entity
	 */
	public CmsContent getContentEntity();
	
	/**
	 * 获取内容扩展Entitiy
	 */
	public T getExtendEntity();
	
	/**
	 * 新建内容
	 */
	public Long add();
	
	/**
	 * 更新内容信息
	 */
	public Long save();

	/**
	 * 删除内容
	 */
	public void delete();

	/**
	 * 备份数据到备份表
	 */
	public void backup();

	/**
	 * 发布内容
	 */
	public boolean publish();

	/**
	 * 获取操作人信息
	 */
	LoginUser getOperator();

	/**
	 * 设置操作人信息
	 * 
	 * @param sysUser
	 */
	public void setOperator(LoginUser operator);

	/**
	 * 复制内容到指定栏目
	 * 
	 * @param catalog
	 * @param copyType
	 * @return
	 */
	public void copyTo(CmsCatalog catalog, Integer copyType);

	/**
	 * 转移内容到指定栏目
	 * 
	 * @param catalog
	 * @return
	 */
	public void moveTo(CmsCatalog catalog);

	/**
	 * 自定义参数，扩展用
	 */
	public Map<String, Object> getParams();

	/**
	 * 设置自定义参数
	 * 
	 * @param params
	 */
	public void setParams(Map<String, Object> params);

	/**
	 * 置顶
	 * @return
	 */
	public void setTop(LocalDateTime topEndTime);

	/**
	 * 取消置顶
	 * @return
	 */
	public void cancelTop();

	/**
	 * 排序
	 * @return
	 */
	public void sort(Long targetContentId);

	/**
	 * 下线
	 * @return
	 */
	public void offline();

	/**
	 * 归档
	 * @return
	 */
	public void archive();

	/**
	 * 全文检索分词内容
	 * 
	 * @return
	 */
	public String getFullText();

	/**
	 * 是否有扩展表
	 */
	boolean hasExtendEntity();
}
