package com.ruoyi.contentcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.core.impl.CatalogType_Common;
import com.ruoyi.contentcore.core.impl.InternalDataType_Content;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsPublishPipe;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.CopyContentDTO;
import com.ruoyi.contentcore.domain.dto.MoveContentDTO;
import com.ruoyi.contentcore.domain.dto.SetTopContentDTO;
import com.ruoyi.contentcore.domain.dto.SortContentDTO;
import com.ruoyi.contentcore.domain.vo.RecycleContentVO;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.fixed.config.BackendContext;
import com.ruoyi.contentcore.listener.event.AfterContentDeleteEvent;
import com.ruoyi.contentcore.listener.event.AfterContentOfflineEvent;
import com.ruoyi.contentcore.listener.event.AfterContentSaveEvent;
import com.ruoyi.contentcore.listener.event.BeforeContentSaveEvent;
import com.ruoyi.contentcore.mapper.CmsContentMapper;
import com.ruoyi.contentcore.perms.CatalogPermissionType.CatalogPrivItem;
import com.ruoyi.contentcore.properties.RepeatTitleCheckProperty;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;
import com.ruoyi.system.permission.PermissionUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl extends ServiceImpl<CmsContentMapper, CmsContent> implements IContentService {

	private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

	private final ApplicationContext applicationContext;
	
	private final CmsContentMapper contentMapper;

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IPublishPipeService publishPipeService;

	private final AsyncTaskManager asyncTaskManager;
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteContents(List<Long> contentIds, LoginUser operator) {
		for (Long contentId : contentIds) {
			CmsContent xContent = this.getById(contentId);
			Assert.notNull(xContent, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));
			PermissionUtils.checkPermission(CatalogPrivItem.DeleteContent.getPermissionKey(xContent.getCatalogId()), operator);

			IContentType contentType = ContentCoreUtils.getContentType(xContent.getContentType());
			IContent<?> content = contentType.loadContent(xContent);
			content.setOperator(operator);
			content.delete();

			applicationContext.publishEvent(new AfterContentDeleteEvent(this, content));
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recoverContents(List<Long> backupIds, LoginUser operator) {
		List<RecycleContentVO> backupContents = this.contentMapper.selectRecycleContentByBackupIds(backupIds);
		for (RecycleContentVO backupContent : backupContents) {
			IContentType contentType = ContentCoreUtils.getContentType(backupContent.getContentType());
			contentType.recover(backupContent.getContentId());
		}
		backupIds.forEach(backupId -> {
			this.recover(backupId, CmsContent.class);
		});
	}
	
	@Override
	public void deleteRecycleContents(List<Long> backupIds) {
		List<RecycleContentVO> backupContents = this.contentMapper.selectRecycleContentByBackupIds(backupIds);
		for (RecycleContentVO backupContent : backupContents) {
			IContentType contentType = ContentCoreUtils.getContentType(backupContent.getContentType());
			contentType.deleteBackups(backupContent.getContentId());
		}
		this.deleteBackups(backupIds, CmsContent.class);
	}

	@Override
	public boolean deleteContentsByCatalog(CmsCatalog catalog, LoginUser operator) {
		int pageSize = 100;
		long total = this.lambdaQuery().likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors()).count();

		for (int i = 0; i * pageSize < total; i++) {
			AsyncTaskManager.setTaskProgressInfo((int) (i * pageSize / total),
					"正在栏目删除内容：" + (i * pageSize) + " / " + total);
			this.lambdaQuery().likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors())
					.page(new Page<>(i, pageSize, false)).getRecords().forEach(content -> {
						IContentType contentType = ContentCoreUtils.getContentType(content.getContentType());
						IContent<?> icontent = contentType.loadContent(content);
						icontent.setOperator(operator);
						icontent.delete();
					});
		}
		return false;
	}

	@Override
	public String getContentLink(CmsContent content, int pageIndex, String publishPipeCode, boolean isPreview) {
		if (content.isLinkContent()) {
			return InternalUrlUtils.getActualUrl(content.getRedirectUrl(), publishPipeCode, isPreview);
		}
		if (isPreview) {
			String previewPath = IInternalDataType.getPreviewPath(InternalDataType_Content.ID, content.getContentId(),
					publishPipeCode, pageIndex);
			return BackendContext.getValue() + previewPath;
		}

		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		if (catalog.isStaticize()) {
			CmsSite site = this.siteService.getSite(content.getSiteId());
			String contentPath = content.getStaticPath();
			if (StringUtils.isEmpty(contentPath)) {
				contentPath = catalog.getPath() + content.getContentId() + "." + site.getStaticSuffix(publishPipeCode);
			}
			return site.getUrl(publishPipeCode) + contentPath;
		} else {
			String viewPath = IInternalDataType.getViewPath(InternalDataType_Content.ID, content.getContentId(),
					publishPipeCode, pageIndex);
			return BackendContext.getValue() + viewPath;
		}
	}

	@Override
	public void lock(Long contentId, String operator) {
		CmsContent content = this.getById(contentId);
		Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));
		boolean checkLock = content.isLock() && StringUtils.isNotEmpty(content.getLockUser())
				&& !StringUtils.equals(content.getLockUser(), operator);
		Assert.isFalse(checkLock, () -> ContentCoreErrorCode.CONTENT_LOCKED.exception(content.getLockUser()));

		content.setIsLock(YesOrNo.YES);
		content.setLockUser(operator);
		content.updateBy(operator);
		this.updateById(content);
	}

	@Override
	public void unLock(Long contentId, String operator) {
		CmsContent content = this.getById(contentId);
		Assert.notNull(content, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));
		if (!content.isLock()) {
			return;
		}
		boolean checkOp = StringUtils.isNotEmpty(content.getLockUser())
				&& !StringUtils.equals(content.getLockUser(), operator);
		Assert.isFalse(checkOp, () -> ContentCoreErrorCode.CONTENT_LOCKED.exception(content.getLockUser()));
		content.setIsLock(YesOrNo.NO);
		content.setLockUser(StringUtils.EMPTY);
		content.updateBy(operator);
		this.updateById(content);
	}

	@Override
	public AsyncTask addContent(IContent<?> content) {
		ContentServiceImpl aopProxy = SpringUtils.getAopProxy(this);
		AsyncTask task = new AsyncTask() {

			@Override
			public void run0() {
				aopProxy.addContent0(content);
			}
		};
		task.setType("SaveContent");
		asyncTaskManager.execute(task);
		return task;
	}

	@Transactional(rollbackFor = Exception.class)
	public void addContent0(IContent<?> content) {
		applicationContext.publishEvent(new BeforeContentSaveEvent(this, content));
		content.add();
		applicationContext.publishEvent(new AfterContentSaveEvent(this, content));
		AsyncTaskManager.setTaskPercent(100);
	}

	@Override
	public AsyncTask saveContent(IContent<?> content) {
		AsyncTask task = new AsyncTask() {

			@Override
			public void run0() {
				saveContent0(content);
			}
		};
		task.setType("SaveContent");
		asyncTaskManager.execute(task);
		return task;
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveContent0(IContent<?> content) {
		applicationContext.publishEvent(new BeforeContentSaveEvent(this, content));
		content.save();
		applicationContext.publishEvent(new AfterContentSaveEvent(this, content));
		AsyncTaskManager.setTaskPercent(100);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void copy(CopyContentDTO dto) {
		List<Long> contentIds = dto.getContentIds();
		for (Long contentId : contentIds) {
			CmsContent cmsContent = this.getById(contentId);
			Assert.notNull(cmsContent, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

			Long[] catalogIds = dto.getCatalogIds().stream().filter(id -> id != cmsContent.getCatalogId())
					.toArray(Long[]::new);
			for (Long catalogId : catalogIds) {
				CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
				if (catalog == null) {
					continue; // 目标栏目错误直接跳过
				}
				if (!catalog.getCatalogType().equals(CatalogType_Common.ID)) {
					continue; // 非普通栏目不能复制
				}
				IContentType ct = ContentCoreUtils.getContentType(cmsContent.getContentType());
				IContent<?> content = ct.loadContent(cmsContent);
				content.setOperator(dto.getOperator());
				content.copyTo(catalog, dto.getCopyType());
			}
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void move(MoveContentDTO dto) {
		List<Long> contentIds = dto.getContentIds();
		for (Long contentId : contentIds) {
			CmsContent cmsContent = this.getById(contentId);
			Assert.notNull(cmsContent, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("contentId", contentId));

			if (cmsContent.getCatalogId().equals(dto.getCatalogId())) {
				continue;
			}
			CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
			Assert.notNull(catalog,
					() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", dto.getCatalogId()));

			if (!catalog.getCatalogType().equals(CatalogType_Common.ID)) {
				continue;
			}
			IContentType ct = ContentCoreUtils.getContentType(cmsContent.getContentType());
			IContent<?> content = ct.loadContent(cmsContent);
			content.setOperator(dto.getOperator());
			content.moveTo(catalog);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void setTop(SetTopContentDTO dto) {
		List<CmsContent> contents = this.listByIds(dto.getContentIds());
		for (CmsContent c : contents) {
			IContentType ct = ContentCoreUtils.getContentType(c.getContentType());
			IContent<?> content = ct.loadContent(c);
			content.setOperator(dto.getOperator());
			content.setTop(dto.getTopEndTime());
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancelTop(List<Long> contentIds, LoginUser operator) {
		List<CmsContent> contents = this.listByIds(contentIds);
		for (CmsContent c : contents) {
			IContentType ct = ContentCoreUtils.getContentType(c.getContentType());
			IContent<?> content = ct.loadContent(c);
			content.setOperator(operator);
			content.cancelTop();
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void offline(List<Long> contentIds, LoginUser operator) {
		List<CmsContent> contents = this.listByIds(contentIds);
		for (CmsContent c : contents) {
			IContentType ct = ContentCoreUtils.getContentType(c.getContentType());
			IContent<?> content = ct.loadContent(c);
			content.setOperator(operator);
			content.offline();

			this.applicationContext.publishEvent(new AfterContentOfflineEvent(this, content));
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sort(SortContentDTO dto) {
		CmsContent c = this.getById(dto.getContentId());
		IContentType ct = ContentCoreUtils.getContentType(c.getContentType());
		IContent<?> content = ct.loadContent(c);
		content.setOperator(dto.getOperator());
		content.sort(dto.getTargetContentId());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void archive(List<Long> contentIds, LoginUser operator) {

	}

	@Override
	public boolean checkSameTitle(Long siteId, Long catalogId, Long contentId, String title) {
		CmsSite site = this.siteService.getSite(siteId);

		String repeatTitleCheckType = RepeatTitleCheckProperty.getValue(site.getConfigProps());

		if (StringUtils.isNotEmpty(repeatTitleCheckType)) {
			if (RepeatTitleCheckProperty.CheckType_Site.equals(repeatTitleCheckType)) {
				LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
						.eq(CmsContent::getSiteId, siteId).eq(CmsContent::getTitle, title)
						.ne(contentId != null && contentId > 0, CmsContent::getContentId, contentId);
				return this.count(q) > 0;
			} else if (RepeatTitleCheckProperty.CheckType_Catalog.equals(repeatTitleCheckType)) {
				LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
						.eq(CmsContent::getCatalogId, catalogId).eq(CmsContent::getTitle, title)
						.ne(contentId != null && contentId > 0, CmsContent::getContentId, contentId);
				return this.count(q) > 0;
			}
		}
		return false;
	}

	@Override
	public void deleteStaticFiles(CmsContent content) throws IOException {
		if (content.isLinkContent()) {
			return;
		}
		CmsCatalog catalog = this.catalogService.getCatalog(content.getCatalogId());
		String path = content.getStaticPath();
		if (StringUtils.isEmpty(path)) {
			path = catalog.getPath() + content.getContentId() + StringUtils.DOT;
		}

		CmsSite site = this.siteService.getSite(content.getSiteId());
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(site.getSiteId());
		for (CmsPublishPipe publishPipe : publishPipes) {
			String siteRoot = SiteUtils.getSiteRoot(site, publishPipe.getCode());
			String filePath = siteRoot + path + site.getStaticSuffix(publishPipe.getCode());
			File file = new File(filePath);
			if (file.exists()) {
				FileUtils.delete(file);
			}
			logger.debug("删除内容静态文件：" + filePath);
		}
	}
}
