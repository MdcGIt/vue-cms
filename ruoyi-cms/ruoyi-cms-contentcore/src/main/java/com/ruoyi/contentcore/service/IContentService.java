package com.ruoyi.contentcore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.dto.CopyContentDTO;
import com.ruoyi.contentcore.domain.dto.MoveContentDTO;
import com.ruoyi.contentcore.domain.dto.SetTopContentDTO;
import com.ruoyi.contentcore.domain.dto.SortContentDTO;
import com.ruoyi.contentcore.mapper.CmsContentMapper;

import java.io.IOException;
import java.util.List;

public interface IContentService extends IService<CmsContent> {

	/**
	 * 添加内容
	 * 
	 * @param content
	 * @return
	 */
	public AsyncTask addContent(IContent<?> content);


	public void addContent0(IContent<?> content);
	
	/**
	 * 更新内容
	 * 
	 * @param content
	 * @return
	 */
	public AsyncTask saveContent(IContent<?> content);

	/**
	 * 删除内容
	 * 
	 * @param contentIds
	 * @return
	 */
	public void deleteContents(List<Long> contentIds, LoginUser operator);


	/**
	 * 恢复回收站删除的内容到指定栏目
	 * 
	 * @param contentIds
	 * @param operator
	 */
	void recoverContents(List<Long> contentIds, LoginUser operator);
	
	/**
	 * 删除指定栏目内容
	 * 
	 * @param catalog
	 * @return
	 */
	public boolean deleteContentsByCatalog(CmsCatalog catalog, LoginUser operator);

	/**
	 * 获取内容链接
	 * 
	 * @param content
	 * @param publishPipeCode
	 * @param isPreview
	 * @return
	 */
	public String getContentLink(CmsContent content, int pageIndex, String publishPipeCode, boolean isPreview);

	/**
	 * 锁定内容
	 * 
	 * @param contentId
	 * @param operator
	 * @return
	 */
	public void lock(Long contentId, String operator);

	/**
	 * 解锁内容
	 * 
	 * @param contentId
	 * @param operator
	 * @return
	 */
	public void unLock(Long contentId, String operator);

	/**
	 * 复制内容
	 * 
	 * @param dto
	 * @return
	 */
	public void copy(CopyContentDTO dto);

	/**
	 * 转移内容
	 * 
	 * @param dto
	 * @return
	 */
	public void move(MoveContentDTO dto);

	/**
	 * 校验重复标题，存在重复标题返回true
	 *
	 * @param siteId
	 * @param catalogId
	 * @param contentId
	 * @param title
	 * @return
	 */
	public boolean checkSameTitle(Long siteId, Long catalogId, Long contentId, String title);

	/**
	 * 置顶
	 * 
	 * @param dto
	 * @return
	 */
	void setTop(SetTopContentDTO dto);

	/**
	 * 取消置顶
	 * 
	 * @param contentIds
	 * @return
	 */
	void cancelTop(List<Long> contentIds, LoginUser operator);

	/**
	 * 下线内容
	 * 
	 * @param contentIds
	 * @return
	 */
	void offline(List<Long> contentIds, LoginUser operator);

	/**
	 * 排序，将指定内容排到目标内容之前
	 * 
	 * @param dto
	 * @return
	 */
	void sort(SortContentDTO dto);

	/**
	 * 归档
	 * 
	 * @param contentIds
	 * @return
	 */
	public void archive(List<Long> contentIds, LoginUser operator);

	/**
	 * 删除指定内容静态文件
	 * 
	 * @param contentEntity
	 * @throws IOException 
	 */
	public void deleteStaticFiles(CmsContent contentEntity) throws IOException;

	/**
	 * 删除备份表数据
	 * 
	 * @param backupIds
	 */
	public void deleteRecycleContents(List<Long> backupIds);

    CmsContentMapper getContentMapper();
}
