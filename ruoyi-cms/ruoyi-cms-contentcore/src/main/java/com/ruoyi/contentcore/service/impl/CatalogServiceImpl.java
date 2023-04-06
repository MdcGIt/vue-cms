package com.ruoyi.contentcore.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.SortUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.ContentCoreConsts;
import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.core.impl.CatalogType_Link;
import com.ruoyi.contentcore.core.impl.InternalDataType_Catalog;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.CatalogApplyChildrenDTO;
import com.ruoyi.contentcore.domain.dto.CatalogDTO;
import com.ruoyi.contentcore.domain.dto.PublishPipeProp;
import com.ruoyi.contentcore.domain.dto.SiteDefaultTemplateDTO;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.fixed.config.BackendContext;
import com.ruoyi.contentcore.listener.event.AfterCatalogDeleteEvent;
import com.ruoyi.contentcore.listener.event.AfterCatalogMoveEvent;
import com.ruoyi.contentcore.listener.event.AfterCatalogSaveEvent;
import com.ruoyi.contentcore.listener.event.BeforeCatalogDeleteEvent;
import com.ruoyi.contentcore.mapper.CmsCatalogMapper;
import com.ruoyi.contentcore.mapper.CmsContentMapper;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.CatalogUtils;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl extends ServiceImpl<CmsCatalogMapper, CmsCatalog> implements ICatalogService {

	public static final String CACHE_PREFIX_ID = "cms:catalog:id:";

	public static final String CACHE_PREFIX_ALIAS = "cms:catalog:alias:";

	private final ApplicationContext applicationContext;

	private final ISiteService siteService;

	private final RedisCache redisCache;

	private final CmsCatalogMapper catalogMapper;

	private final CmsContentMapper contentMapper;

	private final AsyncTaskManager asyncTaskManager;

	@Override
	public CmsCatalog getCatalog(Long catalogId) {
		if (!IdUtils.validate(catalogId)) {
			return null;
		}
		CmsCatalog catalog = this.redisCache.getCacheObject(CACHE_PREFIX_ID + catalogId);
		if (Objects.isNull(catalog)) {
			catalog = this.getById(catalogId);
			if (Objects.nonNull(catalog)) {
				this.setCatalogCache(catalog);
			}
		}
		return catalog;
	}

	@Override
	public CmsCatalog getCatalogByAlias(Long siteId, String catalogAlias) {
		if (!IdUtils.validate(siteId) || StringUtils.isEmpty(catalogAlias)) {
			return null;
		}
		Assert.notNull(catalogAlias, () -> CommonErrorCode.NOT_EMPTY.exception("CatalogAlias: " + catalogAlias));
		CmsCatalog catalog = this.redisCache.getCacheObject(CACHE_PREFIX_ALIAS + siteId + ":" + catalogAlias);
		if (Objects.isNull(catalog)) {
			catalog = this.lambdaQuery().eq(CmsCatalog::getSiteId, siteId).eq(CmsCatalog::getAlias, catalogAlias).one();
			if (Objects.nonNull(catalog)) {
				this.setCatalogCache(catalog);
			}
		}
		return catalog;
	}

	@Override
	public boolean checkCatalogUnique(Long siteId, Long catalogId, String name, String alias, String path) {
		LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId, siteId)
				.and(wrapper -> wrapper.eq(CmsCatalog::getName, name).or().eq(CmsCatalog::getAlias, alias).or()
						.eq(CmsCatalog::getPath, path))
				.ne(catalogId != null && catalogId > 0, CmsCatalog::getCatalogId, catalogId);
		return this.count(q) == 0;
	}

	@Override
	public List<TreeNode<String>> buildCatalogTreeData(List<CmsCatalog> catalogs) {
		if (Objects.isNull(catalogs)) {
			return List.of();
		}
		List<TreeNode<String>> list = catalogs.stream().map(c -> {
			TreeNode<String> treeNode = new TreeNode<>(String.valueOf(c.getCatalogId()),
					String.valueOf(c.getParentId()), c.getName(), c.getParentId() == 0);
			String internalUrl = InternalUrlUtils.getInternalUrl(InternalDataType_Catalog.ID, c.getCatalogId());
			String logoSrc = InternalUrlUtils.getActualPreviewUrl(c.getLogo());
			Map<String, Object> props = Map.of("path", c.getPath(), "internalUrl", internalUrl, "logo",
					c.getLogo() == null ? "" : c.getLogo(), "logoSrc", logoSrc == null ? "" : logoSrc, "description",
					c.getDescription() == null ? "" : c.getDescription());
			treeNode.setProps(props);
			return treeNode;
		}).toList();
		return TreeNode.build(list);
	}

	@Override
	public CmsCatalog addCatalog(CatalogDTO dto) throws IOException {
		boolean checkCatalogUnique = this.checkCatalogUnique(dto.getSiteId(), null, dto.getName(), dto.getAlias(),
				dto.getPath());
		Assert.isTrue(checkCatalogUnique, ContentCoreErrorCode.CONFLICT_CATALOG::exception);

		if (!dto.getPath().endsWith("/")) {
			dto.setPath(dto.getPath() + "/");
		}
		if (dto.getParentId() == null) {
			dto.setParentId(0L);
		}
		CmsCatalog catalog = new CmsCatalog();
		BeanUtils.copyProperties(dto, catalog, "catalogId");
		catalog.setCatalogId(IdUtils.getSnowflakeId());
		catalog.setTreeLevel(1);
		String parentAncestors = StringUtils.EMPTY;
		if (catalog.getParentId() > 0) {
			CmsCatalog parentCatalog = this.getById(catalog.getParentId());
			boolean maxTreeLevelFlag = parentCatalog.getTreeLevel() + 1 <= ContentCoreConsts.CATALOG_MAX_TREE_LEVEL;
			Assert.isTrue(maxTreeLevelFlag, ContentCoreErrorCode.CATALOG_MAX_TREE_LEVEL::exception);

			catalog.setTreeLevel(parentCatalog.getTreeLevel() + 1);
			parentCatalog.setChildCount(parentCatalog.getChildCount() + 1);
			this.updateById(parentCatalog);

			parentAncestors = parentCatalog.getAncestors();
		}
		catalog.setAncestors(CatalogUtils.getCatalogAncestors(parentAncestors, catalog.getCatalogId()));
		catalog.setSortFlag(SortUtils.getDefaultSortValue());
		catalog.setContentCount(0);
		catalog.setChildCount(0);
		catalog.setStaticFlag(YesOrNo.YES);
		catalog.setVisibleFlag(YesOrNo.YES);
		catalog.createBy(dto.getOperator().getUsername());
		this.save(catalog);

		return catalog;
	}

	@Override
	public CmsCatalog editCatalog(CatalogDTO dto) throws IOException {
		boolean checkCatalogUnique = this.checkCatalogUnique(dto.getSiteId(), dto.getCatalogId(), dto.getName(),
				dto.getAlias(), dto.getPath());
		Assert.isTrue(checkCatalogUnique, ContentCoreErrorCode.CONFLICT_CATALOG::exception);

		CmsCatalog catalog = this.getById(dto.getCatalogId());
		String oldPath = catalog.getPath();
		BeanUtils.copyProperties(dto, catalog, "siteId", "catalogId", "parentId", "innercode", "treeLevel",
				"childCount", "contentCount", "hitCount");
		// 发布通道数据处理
		Map<String, Map<String, Object>> publishPipeProps = dto.getPublishPipeDatas().stream()
				.collect(Collectors.toMap(PublishPipeProp::getPipeCode, PublishPipeProp::getProps));
		catalog.setPublishPipeProps(publishPipeProps);
		catalog.updateBy(dto.getOperator().getUsername());
		this.updateById(catalog);

		this.clearCache(catalog);
		this.applicationContext.publishEvent(new AfterCatalogSaveEvent(this, catalog, oldPath, dto.getParams()));
		return catalog;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public CmsCatalog deleteCatalog(long catalogId) {
		CmsCatalog catalog = this.getById(catalogId);
		applicationContext.publishEvent(new BeforeCatalogDeleteEvent(this, catalog));

		AsyncTaskManager.setTaskMessage("正在删除栏目数据");
		long childCount = lambdaQuery().eq(CmsCatalog::getParentId, catalog.getCatalogId()).count();
		Assert.isTrue(childCount == 0, ContentCoreErrorCode.DEL_CHILD_FIRST::exception);

		if (catalog.getParentId() > 0) {
			CmsCatalog parentCatalog = getById(catalog.getParentId());
			parentCatalog.setChildCount(parentCatalog.getChildCount() - 1);
			updateById(parentCatalog);
		}
		// 删除栏目
		removeById(catalogId);
		// 清除缓存
		clearCache(catalog);
		applicationContext.publishEvent(new AfterCatalogDeleteEvent(this, catalog));
		return catalog;
	}

	@Override
	public String getCatalogLink(CmsCatalog catalog, int pageIndex, String publishPipeCode, boolean isPreview) {
		if (catalog.getCatalogType().equals(CatalogType_Link.ID)) {
			return InternalUrlUtils.getActualUrl(catalog.getRedirectUrl(), publishPipeCode, isPreview);
		}
		if (isPreview) {
			String catalogPath = IInternalDataType.getPreviewPath(InternalDataType_Catalog.ID, catalog.getCatalogId(),
					publishPipeCode, pageIndex);
			return BackendContext.getValue() + catalogPath;
		}
		if (catalog.isStaticize()) {
			CmsSite site = this.siteService.getSite(catalog.getSiteId());
			return site.getUrl(publishPipeCode) + catalog.getPath();
		} else {
			String catalogPath = IInternalDataType.getViewPath(InternalDataType_Catalog.ID, catalog.getCatalogId(),
					publishPipeCode, pageIndex);
			return BackendContext.getValue() + catalogPath;
		}
	}

	@Override
	public void applyChildren(CatalogApplyChildrenDTO dto) {
		CmsCatalog catalog = this.getCatalog(dto.getCatalogId());

		LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<>();
		if (dto.getToCatalogIds() != null && dto.getToCatalogIds().size() > 0) {
			q.in(CmsCatalog::getCatalogId, dto.getToCatalogIds());
		} else {
			q.likeRight(CmsCatalog::getAncestors, catalog.getAncestors() + CatalogUtils.ANCESTORS_SPLITER);
		}
		List<CmsCatalog> toCatalogs = this.list(q);
		for (CmsCatalog toCatalog : toCatalogs) {
			if (dto.isAllExtends()) {
				toCatalog.setConfigProps(catalog.getConfigProps());
			} else if (dto.getConfigPropKeys() != null) {
				dto.getConfigPropKeys().forEach(propKey -> {
					toCatalog.getConfigProps().put(propKey, catalog.getConfigProps().get(propKey));
				});
			} else if (StringUtils.isNotEmpty(dto.getPublishPipeCode()) && dto.getPublishPipePropKeys() != null
					&& dto.getPublishPipePropKeys().size() > 0) {
				Map<String, Object> publishPipeProps = toCatalog.getPublishPipeProps(dto.getPublishPipeCode());
				for (String propKey : dto.getPublishPipePropKeys()) {
					publishPipeProps.put(propKey, catalog.getPublishPipeProps(dto.getPublishPipeCode()).get(propKey));
				}
			}
			toCatalog.updateBy(dto.getOperator().getUsername());
			this.clearCache(toCatalog);
		}
		this.updateBatchById(toCatalogs);
	}

	@Override
	public void applySiteDefaultTemplateToCatalog(SiteDefaultTemplateDTO dto) {
		CmsSite site = this.siteService.getSite(dto.getSiteId());

		LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<CmsCatalog>().in(CmsCatalog::getCatalogId,
				dto.getToCatalogIds());
		List<CmsCatalog> toCatalogs = this.list(q);
		if (toCatalogs.size() > 0) {
			for (CmsCatalog toCatalog : toCatalogs) {
				List<PublishPipeProp> publishPipeProps = dto.getPublishPipeProps();
				for (PublishPipeProp publishPipeProp : publishPipeProps) {
					Map<String, Object> sitePublishPipeProp = site.getPublishPipeProps()
							.get(publishPipeProp.getPipeCode());
					Map<String, Object> catalogPublishPipeProp = toCatalog
							.getPublishPipeProps(publishPipeProp.getPipeCode());
					publishPipeProp.getProps().keySet().forEach(key -> {
						catalogPublishPipeProp.put(key, sitePublishPipeProp.get(key));
					});
				}
				toCatalog.updateBy(dto.getOperator().getUsername());
			}
			this.updateBatchById(toCatalogs);
		}
	}

	@Override
	public void clearCache(CmsCatalog catalog) {
		this.redisCache.deleteObject(CACHE_PREFIX_ID + catalog.getCatalogId());
		this.redisCache.deleteObject(CACHE_PREFIX_ALIAS + catalog.getSiteId() + ":" + catalog.getAlias());
	}
	
	private void setCatalogCache(CmsCatalog catalog) {
		this.redisCache.setCacheObject(CACHE_PREFIX_ID + catalog.getCatalogId(), catalog);
		this.redisCache.setCacheObject(CACHE_PREFIX_ALIAS + catalog.getSiteId() + ":" + catalog.getAlias(), catalog);
	}

	@Override
	public void changeVisible(Long catalogId, String visible) {
		CmsCatalog catalog = this.getCatalog(catalogId);
		if (StringUtils.equals(visible, catalog.getVisibleFlag())) {
			return;
		}
		catalog.setVisibleFlag(YesOrNo.isYes(visible) ? YesOrNo.YES : YesOrNo.NO);
		this.updateById(catalog);
		this.clearCache(catalog);
	}

	@Override
	public AsyncTask moveCatalog(CmsCatalog fromCatalog, CmsCatalog toCatalog) {
		// 所有需要迁移的子栏目，按Ancestors排序依次处理
		List<CmsCatalog> children = this.lambdaQuery().ne(CmsCatalog::getCatalogId, fromCatalog.getCatalogId())
				.likeRight(CmsCatalog::getAncestors, fromCatalog.getAncestors()).orderByAsc(CmsCatalog::getAncestors)
				.list();
		// 判断栏目ancestors长度是否会超过限制
		int baseTreeLevel = Objects.isNull(toCatalog) ? 1 : toCatalog.getTreeLevel() + 1;
		int maxTreelevel = baseTreeLevel;
		for (CmsCatalog catalog : children) {
			maxTreelevel = Math.max(maxTreelevel, catalog.getTreeLevel() - fromCatalog.getTreeLevel() + baseTreeLevel);
		}
		Assert.isTrue(ContentCoreConsts.CATALOG_MAX_TREE_LEVEL >= maxTreelevel,
				ContentCoreErrorCode.CATALOG_MAX_TREE_LEVEL::exception);
		AsyncTask task = new AsyncTask() {

			@Override
			public void run0() throws Exception {
				moveCatalog0(fromCatalog, toCatalog, children);
			}
		};
		// 设置唯一任务ID避免同步执行，可能会导致数据错乱。
		task.setTaskId("CatalogMove");
		this.asyncTaskManager.execute(task);
		return task;
	}

	private void moveCatalog0(CmsCatalog fromCatalog, CmsCatalog toCatalog, List<CmsCatalog> children) {
		Map<Long, CmsCatalog> invokedCatalogs = new HashMap<>();
		AsyncTaskManager.setTaskPercent(10);
		// 1、原父级栏目子节点数-1
		if (fromCatalog.getParentId() > 0) {
			AsyncTaskManager.setTaskMessage("更新转移栏目原父级栏目数据");
			CmsCatalog parent = this.getById(fromCatalog.getCatalogId());
			parent.setChildCount(parent.getChildCount() - 1);
			invokedCatalogs.put(parent.getCatalogId(), parent);
		}
		// 2、来源栏目修改相关属性
		AsyncTaskManager.setTaskMessage("更新转移栏目数据");
		fromCatalog.setParentId(Objects.isNull(toCatalog) ? 0 : toCatalog.getCatalogId());
		String fromCatalogAncestors = CatalogUtils.getCatalogAncestors(toCatalog, fromCatalog.getCatalogId());
		fromCatalog.setAncestors(fromCatalogAncestors);
		fromCatalog.setTreeLevel(Objects.isNull(toCatalog) ? 1 : toCatalog.getTreeLevel() + 1);
		fromCatalog.setSortFlag(SortUtils.getDefaultSortValue());
		invokedCatalogs.put(fromCatalog.getCatalogId(), fromCatalog);
		// 3、依次处理所有栏目
		for (int i = 0; i < children.size(); i++) {
			AsyncTaskManager.setTaskMessage("更新转移栏目子栏目数据");
			CmsCatalog child = children.get(i);
			CmsCatalog parent = invokedCatalogs.get(child.getParentId());
			child.setAncestors(CatalogUtils.getCatalogAncestors(parent.getAncestors(), child.getCatalogId()));
			child.setTreeLevel(parent.getTreeLevel() + 1);
			invokedCatalogs.put(child.getCatalogId(), child);
		}
		// 4、目标栏目子栏目数+1
		if (Objects.nonNull(toCatalog)) {
			AsyncTaskManager.setTaskMessage("更新转移目标栏目数据");
			toCatalog.setChildCount(toCatalog.getChildCount() + 1);
			invokedCatalogs.put(toCatalog.getCatalogId(), toCatalog);
		}
		AsyncTaskManager.setTaskPercent(20);
		// 5、批量更新数据库
		invokedCatalogs.values().forEach(catalog -> {
			this.lambdaUpdate().set(CmsCatalog::getParentId, catalog.getParentId())
					.set(CmsCatalog::getAncestors, catalog.getAncestors())
					.set(CmsCatalog::getTreeLevel, catalog.getTreeLevel())
					.set(CmsCatalog::getChildCount, catalog.getChildCount())
					.eq(CmsCatalog::getCatalogId, catalog.getCatalogId()).update();
		});
		AsyncTaskManager.setTaskPercent(30);
		// 6、更新缓存
		invokedCatalogs.values().forEach(c -> clearCache(c));
		// 7、更新除目标栏目外的所有栏目内容数据
		AsyncTaskManager.setTaskProgressInfo(40, "更新转移栏目及其子栏目内容");
		invokedCatalogs.values().forEach(catalog -> {
			if (toCatalog == null || catalog.getCatalogId() != toCatalog.getCatalogId()) {
				new LambdaUpdateChainWrapper<>(contentMapper)
						.set(CmsContent::getCatalogAncestors, catalog.getAncestors())
						.eq(CmsContent::getCatalogId, catalog.getCatalogId()).update();
			}
		});
		// 8、其他扩展，例如：重建栏目内容索引
		this.applicationContext.publishEvent(new AfterCatalogMoveEvent(this, fromCatalog, toCatalog, children));
	}

	@Override
	public void setStaticPath(CmsCatalog catalog, TemplateContext context, boolean hasIndex) {
		CmsSite site = this.siteService.getSite(catalog.getSiteId());
		String siteRoot = SiteUtils.getSiteRoot(site, context.getPublishPipeCode());
		context.setDirectory(siteRoot + catalog.getPath());
		String suffix = site.getStaticSuffix(context.getPublishPipeCode());
		String name = hasIndex ? "list" : "index";
		context.setFirstFileName(name + StringUtils.DOT + suffix);
		context.setOtherFileName(name + "_" + TemplateContext.PlaceHolder_PageNo + StringUtils.DOT + suffix);
	}

	@Override
	public void saveCatalogExtends(Long catalogId, Map<String, String> configs, String operator) {
		CmsCatalog catalog = this.getCatalog(catalogId);
		ConfigPropertyUtils.filterConfigProps(configs, catalog.getConfigProps(), IProperty.UseType.Catalog);

		catalog.setConfigProps(configs);
		catalog.updateBy(operator);
		this.updateById(catalog);
		this.clearCache(catalog);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void sortCatalog(Long catalogId, Integer sort) {
		CmsCatalog catalog = this.getCatalog(catalogId);
		if (sort < 0) {
			// 上移
			List<CmsCatalog> beforeCatalogs = this.lambdaQuery()
					.select(CmsCatalog::getSiteId, CmsCatalog::getCatalogId, CmsCatalog::getAlias,
							CmsCatalog::getSortFlag)
					.eq(CmsCatalog::getSiteId, catalog.getSiteId()).eq(CmsCatalog::getParentId, catalog.getParentId())
					.lt(CmsCatalog::getSortFlag, catalog.getSortFlag()).orderByDesc(CmsCatalog::getSortFlag)
					.page(new Page<>(1, Math.abs(sort), false)).getRecords();
			if (beforeCatalogs.size() == 0) {
				return; // 无需排序
			}
			CmsCatalog targetCatalog = beforeCatalogs.get(beforeCatalogs.size() - 1);
			// 更新排序值
			this.catalogMapper.catalogSortPlusOne(targetCatalog.getSortFlag(), catalog.getSortFlag());
			this.lambdaUpdate().set(CmsCatalog::getSortFlag, targetCatalog.getSortFlag())
					.eq(CmsCatalog::getCatalogId, catalog.getCatalogId()).update();
			beforeCatalogs.forEach(this::clearCache);
		} else {
			// 下移
			List<CmsCatalog> afterCatalogs = this.lambdaQuery()
					.select(CmsCatalog::getSiteId, CmsCatalog::getCatalogId, CmsCatalog::getAlias,
							CmsCatalog::getSortFlag)
					.eq(CmsCatalog::getSiteId, catalog.getSiteId()).eq(CmsCatalog::getParentId, catalog.getParentId())
					.gt(CmsCatalog::getSortFlag, catalog.getSortFlag()).orderByAsc(CmsCatalog::getSortFlag)
					.page(new Page<>(1, sort, false)).getRecords();
			if (afterCatalogs.size() == 0) {
				return; // 无需排序
			}
			CmsCatalog targetCatalog = afterCatalogs.get(afterCatalogs.size() - 1);
			// 更新排序值
			this.catalogMapper.catalogSortMinusOne(catalog.getSortFlag(), targetCatalog.getSortFlag());
			this.lambdaUpdate().set(CmsCatalog::getSortFlag, targetCatalog.getSortFlag())
					.eq(CmsCatalog::getCatalogId, catalog.getCatalogId()).update();
			afterCatalogs.forEach(this::clearCache);
		}
		this.clearCache(catalog);
	}
}
