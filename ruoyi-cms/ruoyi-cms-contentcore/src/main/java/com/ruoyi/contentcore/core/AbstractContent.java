package com.ruoyi.contentcore.core;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.BeanUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.SortUtils;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.enums.ContentCopyType;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.fixed.dict.ContentStatus;
import com.ruoyi.contentcore.properties.PublishedContentEditProperty;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.IPublishService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.CatalogUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;

public abstract class AbstractContent<T> implements IContent<T> {

	private CmsContent content;

	private T extendEntity;

	private CmsSite site;

	private CmsCatalog catalog;

	private ISiteService siteService;

	private ICatalogService catalogService;

	private IContentService contentService;

	private IPublishService publishService;

	private Map<String, Object> params;

	private LoginUser operator;

	@Override
	public CmsSite getSite() {
		if (this.site == null) {
			this.site = this.getSiteService().getSite(this.getSiteId());
		}
		return this.site;
	}

	@Override
	public CmsCatalog getCatalog() {
		if (this.catalog == null) {
			this.catalog = this.getCatalogService().getCatalog(this.getCatalogId());
		}
		return this.catalog;
	}

	@Override
	public Long getSiteId() {
		return this.getContentEntity().getSiteId();
	}

	@Override
	public Long getCatalogId() {
		return this.getContentEntity().getCatalogId();
	}

	@Override
	public String getContentType() {
		return this.getContentEntity().getContentType();
	}

	@Override
	public CmsContent getContentEntity() {
		return content;
	}

	@Override
	public void setContentEntity(CmsContent content) {
		this.content = content;
	}

	@Override
	public T getExtendEntity() {
		return this.extendEntity;
	}

	public void setExtendEntity(T extendEntity) {
		this.extendEntity = extendEntity;
	}

	@Override
	public LoginUser getOperator() {
		return this.operator;
	}

	@Override
	public void setOperator(LoginUser operator) {
		this.operator = operator;
	}

	@Override
	public boolean hasExtendEntity() {
		return !this.getContentEntity().isLinkContent()
				&& !ContentCopyType.isMapping(this.getContentEntity().getCopyType());
	}

	@Override
	public Long add() {
		CmsCatalog catalog = this.getCatalogService().getById(this.getCatalogId());
		if (catalog == null) {
			throw CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", this.getCatalog());
		}
		if (this.getContentService().checkSameTitle(catalog.getSiteId(), catalog.getCatalogId(),
				this.getContentEntity().getContentId(), this.getContentEntity().getTitle())) {
			throw ContentCoreErrorCode.TITLE_REPLEAT.exception();
		}
		content.setSiteId(catalog.getSiteId());
		content.setCatalogAncestors(catalog.getAncestors());
		content.setTopCatalog(CatalogUtils.getTopCatalog(catalog));
		content.setDeptId(this.getOperator().getDeptId());
		content.setContentType(this.getContentType());

		content.setStatus(ContentStatus.DRAFT);
		content.setSortFlag(SortUtils.getDefaultSortValue());
		content.setIsLock(YesOrNo.NO);
		content.createBy(this.getOperator().getUsername());

		// 栏目内容数+1
		catalog.setContentCount(catalog.getContentCount() + 1);
		this.getCatalogService().updateById(catalog);
		return this.getContentEntity().getContentId();
	}

	void checkLock() {
		boolean lockContent = content.isLock() && StringUtils.isNotEmpty(content.getLockUser())
				&& !content.getLockUser().equals(this.getOperator().getUsername());
		Assert.isFalse(lockContent, () -> ContentCoreErrorCode.CONTENT_LOCKED.exception(content.getLockUser()));
	}

	@Override
	public Long save() {
		checkLock();
		if (this.getContentService().checkSameTitle(this.getContentEntity().getSiteId(),
				this.getContentEntity().getCatalogId(), this.getContentEntity().getContentId(),
				this.getContentEntity().getTitle())) {
			throw ContentCoreErrorCode.TITLE_REPLEAT.exception();
		}
		if (ContentStatus.isPublished(content.getStatus())) {
			boolean editPublishedContent = PublishedContentEditProperty.getValue(this.getSite().getConfigProps());
			if (!editPublishedContent) {
				throw ContentCoreErrorCode.CANNOT_EDIT_PUBLISHED_CONTENT.exception();
			}
		}
		if (ContentStatus.isToPublishOrPublished(content.getStatus())) {
			content.setStatus(ContentStatus.EDITING);
		}
		content.updateBy(this.getOperator().getUsername());
		return this.getContentEntity().getContentId();
	}

	@Override
	public void delete() {
		this.checkLock();
		this.getContentService().removeById(this.getContentEntity());
		// 直接删除站内映射内容
		this.getContentService().getContentMapper().deleteMappingIgnoreLogicDel(content.getContentId());
		// 栏目内容数-1
		CmsCatalog catalog = this.getCatalogService().getById(this.getCatalogId());
		catalog.setContentCount(catalog.getContentCount() - 1);
		this.getCatalogService().updateById(catalog);
	}

	@Override
	public boolean publish() {
		checkLock();
		boolean update = false;
		if (content.getPublishDate() == null) {
			content.setPublishDate(LocalDateTime.now());
			update = true;
		} else if (content.getPublishDate().isAfter(LocalDateTime.now())
				&& ContentStatus.isToPublish(content.getStatus())) {
			return false; // 待发布内容并且指定了发布时间的未到发布时间直接跳过
		}
		if (!ContentStatus.isPublished(content.getStatus())) {
			content.setStatus(ContentStatus.PUBLISHED);
			update = true;
		}
		if (update) {
			this.getContentService().updateById(content);
		}
		// 静态化
		this.getPublishService().contentStaticize(this);
		return true;
	}

	@Override
	public void copyTo(CmsCatalog toCatalog, Integer copyType) {
		checkLock();
		if (this.getContentService().checkSameTitle(toCatalog.getSiteId(), toCatalog.getCatalogId(), null,
				this.getContentEntity().getTitle())) {
			throw ContentCoreErrorCode.TITLE_REPLEAT.exception();
		}
		// TODO 校验权限，需要同时拥有目标栏目的新建权限和源栏目的复制权限
		CmsContent newContent = new CmsContent();
		BeanUtils.copyProperties(this.getContentEntity(), newContent, "contentId", "template", "staticPath", "topFlag",
				"topDate", "isLock", "lockUser");
		newContent.setContentId(IdUtils.getSnowflakeId());
		newContent.setCatalogId(toCatalog.getCatalogId());
		newContent.setCatalogAncestors(toCatalog.getAncestors());
		newContent.setTopCatalog(CatalogUtils.getTopCatalog(toCatalog));
		newContent.setDeptCode(toCatalog.getDeptCode());
		newContent.setSortFlag(SortUtils.getDefaultSortValue());
		newContent.createBy(this.getOperator().getUsername());
		newContent.setCopyType(copyType);
		if (content.getCopyId() > 0 && ContentCopyType.isMapping(content.getCopyType())) {
			newContent.setCopyId(content.getCopyId()); // 映射内容溯源
		} else {
			newContent.setCopyId(content.getContentId());
		}
		if (ContentCopyType.isIndependency(copyType)) {
			newContent.setStatus(ContentStatus.DRAFT);
			newContent.setPublishDate(null);
			newContent.setOfflineDate(null);
		}
		this.getContentService().save(newContent);
		this.getParams().put("NewContentId", newContent.getContentId());
		// 栏目内容数+1
		toCatalog.setContentCount(toCatalog.getContentCount() + 1);
		this.getCatalogService().updateById(toCatalog);
	}

	@Override
	public void moveTo(CmsCatalog toCatalog) {
		checkLock();
		if (this.getContentService().checkSameTitle(toCatalog.getSiteId(), toCatalog.getCatalogId(), null,
				this.getContentEntity().getTitle())) {
			throw ContentCoreErrorCode.TITLE_REPLEAT.exception();
		}
		// TODO 校验权限，需要同时拥有目标栏目的新建权限和源栏目的转移权限
		CmsCatalog fromCatalog = this.getCatalogService().getCatalog(content.getCatalogId());
		// 重置内容信息
		content.setSiteId(toCatalog.getSiteId());
		content.setCatalogId(toCatalog.getCatalogId());
		content.setCatalogAncestors(toCatalog.getAncestors());
		content.setTopCatalog(CatalogUtils.getTopCatalog(toCatalog));
		content.setDeptCode(toCatalog.getDeptCode());
		content.setSortFlag(SortUtils.getDefaultSortValue());
		content.setTopFlag(0L);
		content.setPublishPipe(null);
		content.setPublishPipeProps(null);
		// 重置发布状态
		content.setStatus(ContentStatus.DRAFT);
		content.updateBy(this.getOperator().getUsername());
		this.getContentService().updateById(content);
		// 目标栏目内容数量+1
		toCatalog.setContentCount(toCatalog.getContentCount() + 1);
		this.getCatalogService().updateById(toCatalog);
		// 源栏目内容数量-1
		fromCatalog.setContentCount(fromCatalog.getContentCount() - 1);
		this.getCatalogService().updateById(fromCatalog);
	}

	@Override
	public void setTop(LocalDateTime topEndTime) {
		content.setTopFlag(Instant.now().toEpochMilli());
		content.setTopDate(topEndTime);
		content.updateBy(this.getOperator().getUsername());
		this.getContentService().updateById(content);
		// 重新发布内容
		if (ContentStatus.isPublished(this.getContentEntity().getStatus())) {
			this.publish();
		}
	}

	@Override
	public void cancelTop() {
		content.setTopFlag(0L);
		content.setTopDate(null);
		content.updateBy(Objects.nonNull(this.getOperator()) ? this.getOperator().getUsername(): "__System");
		this.getContentService().updateById(content);
		// 重新发布内容
		if (ContentStatus.isPublished(this.getContentEntity().getStatus())) {
			this.publish();
		}
	}

	@Override
	public void sort(Long targetContentId) {
		if (targetContentId.equals(this.getContentEntity().getContentId())) {
			return; // 排序目标是自己直接返回
		}
		checkLock();
		CmsContent next = this.getContentService().getById(targetContentId);
		if (next.getTopFlag() > 0 && this.getContentEntity().getTopFlag() == 0) {
			this.content.setTopFlag(next.getTopFlag() + 1); // 非置顶内容排到置顶内容前需要置顶
		} else if (this.getContentEntity().getTopFlag() > 0 && next.getTopFlag() == 0) {
			this.content.setTopFlag(0L); // 置顶内容排到非置顶内容前取消置顶
			this.content.setTopDate(null);
		}
		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
				.eq(CmsContent::getCatalogId, next.getCatalogId()).gt(CmsContent::getSortFlag, next.getSortFlag())
				.orderByAsc(CmsContent::getSortFlag).last("limit 1");
		CmsContent prev = this.getContentService().getOne(q);
		if (prev == null) {
			this.content.setSortFlag(SortUtils.getDefaultSortValue());
		} else {
			this.content.setSortFlag((next.getSortFlag() + prev.getSortFlag()) / 2);
		}
		this.getContentEntity().updateBy(this.getOperator().getUsername());
		this.getContentService().updateById(content);
	}

	@Override
	public void offline() {
		String status = this.getContentEntity().getStatus();
		this.getContentEntity().setStatus(ContentStatus.OFFLINE);
		this.getContentEntity().updateBy(this.getOperator().getUsername());
		this.getContentService().updateById(this.getContentEntity());

		if (ContentStatus.isPublished(status)) {
			try {
				// 已发布内容删除静态页面
				this.getContentService().deleteStaticFiles(this.getContentEntity());
				// 重新发布内容所在栏目和父级栏目
				String[] catalogIds = this.getContentEntity().getCatalogAncestors()
						.split(CatalogUtils.ANCESTORS_SPLITER);
				for (String catalogId : catalogIds) {
					this.getPublishService().publishCatalog(this.getCatalogService().getCatalog(Long.valueOf(catalogId)),
							false, false, null);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void toPublish() {
		this.getContentEntity().setStatus(ContentStatus.TO_PUBLISHED);
		this.getContentEntity().updateBy(this.getOperator().getUsername());
		this.getContentService().updateById(this.getContentEntity());
	}

	@Override
	public void archive() {
		// TODO 归档
	}

	@Override
	public String getFullText() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.content.getTitle());
		if (StringUtils.isNotEmpty(this.content.getSubTitle())) {
			sb.append(StringUtils.SPACE).append(this.content.getSubTitle());
		}
		if (StringUtils.isNotEmpty(this.content.getShortTitle())) {
			sb.append(StringUtils.SPACE).append(this.content.getShortTitle());
		}
		if (StringUtils.isNotEmpty(this.content.getKeywords())) {
			sb.append(StringUtils.SPACE).append(StringUtils.join(this.content.getKeywords(), StringUtils.SPACE));
		}
		return sb.toString();
	}

	@Override
	public Map<String, Object> getParams() {
		if (this.params == null) {
			this.params = new HashMap<>();
		}
		return this.params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public ISiteService getSiteService() {
		if (this.siteService == null) {
			this.siteService = SpringUtils.getBean(ISiteService.class);
		}
		return this.siteService;
	}

	public ICatalogService getCatalogService() {
		if (this.catalogService == null) {
			this.catalogService = SpringUtils.getBean(ICatalogService.class);
		}
		return this.catalogService;
	}

	public IContentService getContentService() {
		if (this.contentService == null) {
			this.contentService = SpringUtils.getBean(IContentService.class);
		}
		return this.contentService;
	}

	private IPublishService getPublishService() {
		if (this.publishService == null) {
			this.publishService = SpringUtils.getBean(IPublishService.class);
		}
		return this.publishService;
	}
}
