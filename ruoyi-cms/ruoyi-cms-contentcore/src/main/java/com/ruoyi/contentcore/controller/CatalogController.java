package com.ruoyi.contentcore.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.ICatalogType;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.core.IProperty.UseType;
import com.ruoyi.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.CatalogAddDTO;
import com.ruoyi.contentcore.domain.dto.CatalogApplyConfigPropsDTO;
import com.ruoyi.contentcore.domain.dto.CatalogApplyPublishPipeDTO;
import com.ruoyi.contentcore.domain.dto.CatalogUpdateDTO;
import com.ruoyi.contentcore.domain.dto.ChangeCatalogVisibleDTO;
import com.ruoyi.contentcore.domain.dto.PublishCatalogDTO;
import com.ruoyi.contentcore.domain.dto.PublishPipeProp;
import com.ruoyi.contentcore.domain.dto.SortCatalogDTO;
import com.ruoyi.contentcore.domain.vo.CatalogVO;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.perms.CatalogPermissionType.CatalogPrivItem;
import com.ruoyi.contentcore.perms.ContentCorePriv;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.IPublishService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.CmsPrivUtils;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.validator.LongId;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

/**
 * 栏目管理
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Priv(type = AdminUserType.TYPE, value = ContentCorePriv.CatalogView)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/catalog")
public class CatalogController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IPublishPipeService publishPipeService;

	private final IPublishService publishService;

	private final List<ICatalogType> catalogTypes;

	private final List<IContentType> contentTypes;

	private final AsyncTaskManager asyncTaskManager;

	/**
	 * 查询栏目数据列表
	 * 
	 * @param catalog
	 * @return
	 */
	@GetMapping
	public R<?> list() {
		List<CmsCatalog> list = catalogService.lambdaQuery().list().stream().filter(c -> CmsPrivUtils
				.hasCatalogPermission(c.getCatalogId(), CatalogPrivItem.View, StpAdminUtil.getLoginUser())).toList();
		return this.bindDataTable(list);
	}

	/**
	 * 查询栏目详情数据
	 * 
	 * @param catalogId 栏目ID
	 * @return
	 */
	@GetMapping("/{catalogId}")
	public R<?> catalogInfo(@PathVariable @LongId Long catalogId) {
		CmsCatalog catalog = catalogService.getById(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));
		CmsPrivUtils.checkCatalogPermission(catalog.getCatalogId(), CatalogPrivItem.View, StpAdminUtil.getLoginUser());

		CatalogVO dto = CatalogVO.newInstance(catalog);
		CmsSite site = siteService.getById(dto.getSiteId());
		if (StringUtils.isNotEmpty(dto.getLogo())) {
			dto.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(dto.getLogo()));
		}
		// 发布通道数据
		List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(site.getSiteId(),
				PublishPipePropUseType.Catalog, catalog.getPublishPipeProps());
		dto.setPublishPipeDatas(publishPipeProps);
		return R.ok(dto);
	}

	/**
	 * 新增栏目数据
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@Log(title = "新增栏目", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addSave(@RequestBody @Validated CatalogAddDTO dto) throws IOException {
		CmsSite currentSite = this.siteService.getCurrentSite(ServletUtils.getRequest());
		dto.setSiteId(currentSite.getSiteId());
		dto.setOperator(StpAdminUtil.getLoginUser());
		return R.ok(this.catalogService.addCatalog(dto));
	}

	/**
	 * 修改栏目数据
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@Log(title = "编辑栏目", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> editSave(@RequestBody @Validated CatalogUpdateDTO dto) throws IOException {
		CmsPrivUtils.checkCatalogPermission(dto.getCatalogId(), CatalogPrivItem.Edit, StpAdminUtil.getLoginUser());

		dto.setOperator(StpAdminUtil.getLoginUser());
		this.catalogService.editCatalog(dto);
		return R.ok();
	}

	/**
	 * 删除栏目数据
	 * 
	 * @param catalogId 栏目ID
	 * @return
	 */
	@Log(title = "删除", businessType = BusinessType.DELETE)
	@DeleteMapping("/{catalogId}")
	public R<String> remove(@PathVariable("catalogId") @LongId Long catalogId) {
		CmsPrivUtils.checkCatalogPermission(catalogId, CatalogPrivItem.Delete, StpAdminUtil.getLoginUser());

		AsyncTask task = new AsyncTask() {

			@Override
			public void run0() throws Exception {
				catalogService.deleteCatalog(catalogId);
			}
		};
		task.setTaskId("DeleteCatalog_" + catalogId);
		this.asyncTaskManager.execute(task);
		return R.ok(task.getTaskId());
	}

	/**
	 * 显示/隐藏栏目
	 * 
	 * @param dto
	 * @return
	 */
	@Log(title = "显隐栏目", businessType = BusinessType.UPDATE)
	@PutMapping("/visible")
	public R<String> changeVisible(@RequestBody @Validated ChangeCatalogVisibleDTO dto) {
		CmsPrivUtils.checkCatalogPermission(dto.getCatalogId(), CatalogPrivItem.ShowHide, StpAdminUtil.getLoginUser());

		catalogService.changeVisible(dto.getCatalogId(), dto.getVisible());
		return R.ok();
	}

	/**
	 * 栏目树结构数据
	 * 
	 * @return
	 */
	@GetMapping("/treeData")
	public R<?> treeData() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		List<CmsCatalog> catalogs = this.catalogService.lambdaQuery().eq(CmsCatalog::getSiteId, site.getSiteId())
				.orderByAsc(CmsCatalog::getSortFlag).list().stream().filter(c -> CmsPrivUtils
						.hasCatalogPermission(c.getCatalogId(), CatalogPrivItem.View, StpAdminUtil.getLoginUser()))
				.toList();
		List<TreeNode<String>> treeData = catalogService.buildCatalogTreeData(catalogs);
		return R.ok(Map.of("rows", treeData, "siteName", site.getName()));
	}

	/**
	 * 内容类型数据
	 * 
	 * @return
	 */
	@GetMapping("/getContentTypes")
	public R<?> getContentTypes() {
		List<Map<String, String>> list = this.contentTypes.stream().sorted((c1, c2) -> c1.getOrder() - c2.getOrder())
				.map(ct -> Map.of("id", ct.getId(), "name", I18nUtils.get(ct.getName()))).toList();
		return R.ok(list);
	}

	/**
	 * 栏目类型数据
	 * 
	 * @return
	 */
	@GetMapping("/getCatalogTypes")
	public R<?> getCatalogTypes() {
		List<Map<String, String>> list = this.catalogTypes.stream()
				.map(ct -> Map.of("id", ct.getId(), "name", I18nUtils.get(ct.getName()))).toList();
		return R.ok(list);
	}

	/**
	 * 发布栏目
	 * 
	 * @param dto
	 * @return
	 */
	@Log(title = "发布栏目", businessType = BusinessType.OTHER)
	@PostMapping("/publish")
	public R<String> publish(@RequestBody @Validated PublishCatalogDTO dto) {
		CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", dto.getCatalogId()));
		CmsPrivUtils.checkCatalogPermission(catalog.getCatalogId(), CatalogPrivItem.Publish,
				StpAdminUtil.getLoginUser());

		AsyncTask task = this.publishService.publishCatalog(catalog, dto.getPublishChild(), dto.getPublishDetail(),
				dto.getPublishStatus());
		return R.ok(task.getTaskId());
	}

	/**
	 * 获取栏目扩展配置
	 * 
	 * @param catalogId 栏目ID
	 * @return
	 */
	@GetMapping("/extends")
	public R<?> getCatalogExtends(@RequestParam("catalogId") @LongId Long catalogId) {
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));
		CmsPrivUtils.checkCatalogPermission(catalog.getCatalogId(), CatalogPrivItem.View, StpAdminUtil.getLoginUser());

		Map<String, Object> configProps = ConfigPropertyUtils.paseConfigProps(catalog.getConfigProps(),
				UseType.Catalog);
		configProps.put("siteId", catalog.getSiteId());
		configProps.put("PreviewPrefix", SiteUtils.getResourcePrefix(this.siteService.getSite(catalog.getSiteId())));
		return R.ok(configProps);
	}

	/**
	 * 保存栏目扩展配置
	 * 
	 * @param catalogId 栏目ID
	 * @param configs   扩展配置数据
	 * @return
	 */
	@Log(title = "栏目扩展", businessType = BusinessType.UPDATE, isSaveRequestData = false)
	@PutMapping("/extends/{catalogId}")
	public R<?> saveCatalogExtends(@PathVariable("catalogId") @LongId Long catalogId,
			@RequestBody @NotNull Map<String, String> configs) {
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));
		CmsPrivUtils.checkCatalogPermission(catalogId, CatalogPrivItem.Edit, StpAdminUtil.getLoginUser());

		this.catalogService.saveCatalogExtends(catalogId, configs, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	/**
	 * 扩展配置应用到子栏目
	 * 
	 * @param dto
	 * @return
	 */
	@Log(title = "扩展配置2子栏目", businessType = BusinessType.UPDATE)
	@PutMapping("/apply_children/config_props")
	public R<?> applyConfigPropsToChildren(@RequestBody @Validated CatalogApplyConfigPropsDTO dto) {
		CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", dto.getCatalogId()));
		CmsPrivUtils.checkCatalogPermission(dto.getCatalogId(), CatalogPrivItem.Edit, StpAdminUtil.getLoginUser());

		dto.setOperator(StpAdminUtil.getLoginUser());
		this.catalogService.applyConfigPropsToChildren(dto);
		return R.ok();
	}

	/**
	 * 发布通道配置应用到子栏目
	 * 
	 * @param dto
	 * @return
	 */
	@Log(title = "发布通道配置2子栏目", businessType = BusinessType.UPDATE)
	@PutMapping("/apply_children/publish_pipe")
	public R<?> applyPublishPipePropsToChildren(@RequestBody @Validated CatalogApplyPublishPipeDTO dto) {
		CmsCatalog catalog = this.catalogService.getCatalog(dto.getCatalogId());
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", dto.getCatalogId()));
		CmsPrivUtils.checkCatalogPermission(dto.getCatalogId(), CatalogPrivItem.Edit, StpAdminUtil.getLoginUser());

		dto.setOperator(StpAdminUtil.getLoginUser());
		this.catalogService.applyPublishPipePropsToChildren(dto);
		return R.ok();
	}

	/**
	 * 移动栏目
	 * 
	 * @param fromCatalogId 移动的栏目ID
	 * @param toCatalogId   目标栏目ID
	 * @return
	 */
	@Log(title = "移动栏目", businessType = BusinessType.UPDATE)
	@PostMapping("/move/{from}/{to}")
	public R<?> moveCatalog(@PathVariable("from") @LongId Long fromCatalogId,
			@PathVariable("to") @LongId Long toCatalogId) {
		CmsCatalog fromCatalog = this.catalogService.getCatalog(fromCatalogId);
		Assert.notNull(fromCatalog,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("fromCatalogId", fromCatalogId));
		CmsPrivUtils.checkCatalogPermission(fromCatalogId, CatalogPrivItem.Move, StpAdminUtil.getLoginUser());

		CmsCatalog toCatalog = null;
		if (IdUtils.validate(toCatalogId)) {
			toCatalog = this.catalogService.getCatalog(toCatalogId);
			Assert.notNull(toCatalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("toCatalogId", toCatalogId));
			CmsPrivUtils.checkCatalogPermission(toCatalogId, CatalogPrivItem.Move, StpAdminUtil.getLoginUser());

			// 目标栏目不能是源栏目的子栏目或自身，且目标栏目不是当前栏目的父级栏目
			boolean isSelfOfChild = toCatalog.getAncestors().startsWith(fromCatalog.getAncestors())
					|| toCatalog.getCatalogId().equals(fromCatalog.getParentId());
			Assert.isFalse(isSelfOfChild, ContentCoreErrorCode.CATALOG_MOVE_TO_SELF_OR_CHILD::exception);
		}
		AsyncTask task = this.catalogService.moveCatalog(fromCatalog, toCatalog);
		return R.ok(task.getTaskId());
	}

	@Log(title = "栏目排序", businessType = BusinessType.UPDATE)
	@PutMapping("/sort")
	public R<?> sortCatalog(@RequestBody @Validated SortCatalogDTO dto) {
		if (dto.getSort() == 0) {
			return R.fail("排序数值不能为0");
		}
		CmsPrivUtils.checkCatalogPermission(dto.getCatalogId(), CatalogPrivItem.Move, StpAdminUtil.getLoginUser());
		this.catalogService.sortCatalog(dto.getCatalogId(), dto.getSort());
		return R.ok();
	}
}
