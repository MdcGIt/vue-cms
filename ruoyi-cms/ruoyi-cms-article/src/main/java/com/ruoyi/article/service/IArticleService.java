package com.ruoyi.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.article.domain.CmsArticleDetail;
import com.ruoyi.contentcore.domain.CmsSite;

/**
 * 文章服务类
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface IArticleService extends IService<CmsArticleDetail> {

	/**
	 * 保存内部链接
	 * 
	 * 查找内容中所有带iurl属性的标签，如果标签内含有src/href属性则替换成iurl地址，并移除标签iurl属性
	 * 
	 * @param content
	 * @return
	 */
	String saveInternalUrl(String content);

	/**
	 * 下载远程图片
	 * 
	 * @param content
	 * @param siteId
	 * @param operator
	 * @return
	 */
	String downloadRemoteImages(String content, CmsSite site, String operator);

}
