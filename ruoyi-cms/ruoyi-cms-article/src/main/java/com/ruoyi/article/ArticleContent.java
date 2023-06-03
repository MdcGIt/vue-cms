package com.ruoyi.article;

import java.util.regex.Matcher;

import org.springframework.beans.BeanUtils;

import com.ruoyi.article.domain.CmsArticleDetail;
import com.ruoyi.article.properties.AutoArticleLogo;
import com.ruoyi.article.service.IArticleService;
import com.ruoyi.article.service.impl.ArticleServiceImpl;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.utils.HtmlUtils;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.AbstractContent;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.system.fixed.dict.YesOrNo;

public class ArticleContent extends AbstractContent<CmsArticleDetail> {

	private IArticleService articleService;

	@Override
	public Long add() {
		super.add();
		if (!this.hasExtendEntity()) {
			this.getContentService().save(this.getContentEntity());
			return this.getContentEntity().getContentId();
		}
		CmsArticleDetail articleDetail = this.getExtendEntity();
		articleDetail.setContentId(this.getContentEntity().getContentId());
		articleDetail.setSiteId(this.getContentEntity().getSiteId());
		// 处理内部链接
		String contentHtml = this.getArticleService().saveInternalUrl(articleDetail.getContentHtml());
		// 处理文章正文远程图片
		if (YesOrNo.isYes(articleDetail.getDownloadRemoteImage())) {
			AsyncTaskManager.setTaskPercent(90);
			contentHtml = this.getArticleService().downloadRemoteImages(contentHtml, this.getSite(),
					this.getOperator().getUsername());
			articleDetail.setContentHtml(contentHtml);
		}
		// 正文首图作为logo
		if (StringUtils.isEmpty(this.getContentEntity().getLogo())
				&& AutoArticleLogo.getValue(this.getSite().getConfigProps())) {
			this.getContentEntity().setLogo(this.getFirstImage(articleDetail.getContentHtml()));
		}
		this.getContentService().save(this.getContentEntity());
		this.getArticleService().save(articleDetail);
		return this.getContentEntity().getContentId();
	}

	@Override
	public Long save() {
		super.save();
		// 非映射内容或标题内容修改文章详情
		if (!this.hasExtendEntity()) {
			this.getContentService().updateById(this.getContentEntity());
			return this.getContentEntity().getContentId();
		}
		CmsArticleDetail articleDetail = this.getExtendEntity();
		// 处理内部链接
		String contentHtml = this.getArticleService().saveInternalUrl(articleDetail.getContentHtml());
		// 处理文章正文远程图片
		if (YesOrNo.isYes(articleDetail.getDownloadRemoteImage())) {
			AsyncTaskManager.setTaskPercent(90);
			contentHtml = this.getArticleService().downloadRemoteImages(contentHtml, this.getSite(),
					this.getOperator().getUsername());
		}
		articleDetail.setContentHtml(contentHtml);
		// 正文首图作为logo
		if (StringUtils.isEmpty(this.getContentEntity().getLogo())
				&& AutoArticleLogo.getValue(this.getSite().getConfigProps())) {
			this.getContentEntity().setLogo(this.getFirstImage(articleDetail.getContentHtml()));
		}
		this.getContentService().updateById(this.getContentEntity());
		this.getArticleService().updateById(articleDetail);
		return this.getContentEntity().getContentId();
	}

	/**
	 * 获取文章正文第一张图片地址
	 */
	private String getFirstImage(String contentHtml) {
		if (StringUtils.isEmpty(contentHtml)) {
			return contentHtml;
		}
		Matcher matcher = ArticleServiceImpl.ImgHtmlTagPattern.matcher(contentHtml);
		if (matcher.find()) {
			String imgSrc = matcher.group(1);
			if (StringUtils.isNotEmpty(imgSrc)) {
				return imgSrc;
			}
		}
		return null;
	}

	@Override
	public void delete() {
		this.backup();
		super.delete();
		if (this.hasExtendEntity()) {
			this.getArticleService().removeById(this.getContentEntity().getContentId());
		}
	}

	@Override
	public void backup() {
		super.backup();
		if (this.hasExtendEntity()) {
			CmsArticleDetail extendEntity = this.getArticleService().getById(this.getContentEntity().getContentId());
			this.getArticleService().backup(extendEntity, this.getOperator().getUsername());
		}
	}

	@Override
	public void copyTo(CmsCatalog toCatalog, Integer copyType) {
		super.copyTo(toCatalog, copyType);
		if (this.hasExtendEntity()) {
			Long newContentId = (Long) this.getParams().get("NewContentId");
			CmsArticleDetail newArticleDetail = new CmsArticleDetail();
			BeanUtils.copyProperties(this.getExtendEntity(), newArticleDetail, "contentId");
			newArticleDetail.setContentId(newContentId);
			this.getArticleService().save(newArticleDetail);
		}
	}

	@Override
	public String getFullText() {
		return super.getFullText() + " " + HtmlUtils.clean(this.getExtendEntity().getContentHtml());
	}

	public IArticleService getArticleService() {
		if (this.articleService == null) {
			this.articleService = SpringUtils.getBean(IArticleService.class);
		}
		return this.articleService;
	}
}
