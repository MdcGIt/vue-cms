package com.ruoyi.contentcore.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.i18n.I18nUtils;
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
import com.ruoyi.contentcore.domain.dto.CatalogApplyChildrenDTO;
import com.ruoyi.contentcore.domain.dto.CatalogDTO;
import com.ruoyi.contentcore.domain.dto.ChangeCatalogVisibleDTO;
import com.ruoyi.contentcore.domain.dto.PublishCatalogDTO;
import com.ruoyi.contentcore.domain.dto.PublishPipeProp;
import com.ruoyi.contentcore.domain.dto.SortCatalogDTO;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.IPublishService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

/**
 * 栏目管理
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
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
	public R<?> list(CmsCatalog catalog) {
		QueryWrapper<CmsCatalog> queryWrapper = new QueryWrapper<>(catalog);
		List<CmsCatalog> list = catalogService.list(queryWrapper);
		return this.bindDataTable(list);
	}

	/**
	 * 查询栏目详情数据
	 * 
	 * @param catalogId 栏目ID
	 * @return
	 */
	@GetMapping("/{catalogId}")
	public R<?> catalogInfo(@PathVariable Long catalogId) {
		CmsCatalog catalog = catalogService.getById(catalogId);
		if (catalog == null) {
			return R.fail("栏目ID错误：" + catalogId);
		}
		CatalogDTO dto = CatalogDTO.newInstance(catalog);
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
	@PostMapping
	public R<?> addSave(@RequestBody CatalogDTO dto) throws IOException {
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
	@PutMapping
	public R<?> editSave(@RequestBody CatalogDTO dto) throws IOException {
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
	@DeleteMapping("/{catalogId}")
	public R<String> remove(@PathVariable("catalogId") @Min(1) Long catalogId) {
		AsyncTask task = new AsyncTask() {

			@Override
			public void run0() throws Exception {
				catalogService.deleteCatalog(catalogId);
			}
		};
		this.asyncTaskManager.execute(task);
		return R.ok(task.getTaskId());
	}

	/**
	 * 显示/隐藏栏目
	 * 
	 * @param dto
	 * @return
	 */
	@PutMapping("/visible")
	public R<String> changeVisible(@RequestBody ChangeCatalogVisibleDTO dto) {
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
		LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId,
				site.getSiteId()).orderByAsc(CmsCatalog::getSortFlag);
		List<CmsCatalog> catalogs = this.catalogService.list(q);
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
		return R.ok(this.catalogTypes);
	}

	/**
	 * 发布栏目
	 * 
	 * @param publishCatalogDTO
	 * @return
	 */
	@PostMapping("/publish")
	public R<String> publish(@RequestBody PublishCatalogDTO publishCatalogDTO) {
		CmsCatalog catalog = this.catalogService.getCatalog(publishCatalogDTO.getCatalogId());
		AsyncTask task = this.publishService.publishCatalog(catalog, publishCatalogDTO.isPublishChild(),
				publishCatalogDTO.isPublishDetail(), publishCatalogDTO.getPublishStatus());
		return R.ok(task.getTaskId());
	}

	/**
	 * 获取栏目扩展配置
	 * 
	 * @param catalogId 栏目ID
	 * @return
	 */
	@GetMapping("/extends")
	public R<?> getCatalogExtends(@RequestParam("catalogId") @Min(1) Long catalogId) {
		CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
		Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));

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
	@PutMapping("/extends/{catalogId}")
	public R<?> saveCatalogExtends(@PathVariable("catalogId") Long catalogId,
			@RequestBody Map<String, String> configs) {
		this.catalogService.saveCatalogExtends(catalogId, configs, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	/**
	 * 扩展配置应用到子栏目
	 * 
	 * @param dto
	 * @return
	 */
	@PutMapping("/apply_children")
	public R<?> applyChildren(@RequestBody CatalogApplyChildrenDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.catalogService.applyChildren(dto);
		return R.ok();
	}

	/**
	 * 移动栏目
	 * 
	 * @param fromCatalogId 移动的栏目ID
	 * @param toCatalogId   目标栏目ID
	 * @return
	 */
	@PostMapping("/move/{from}/{to}")
	public R<?> moveCatalog(@PathVariable("from") @Min(1) Long fromCatalogId, @PathVariable("to") Long toCatalogId) {
		CmsCatalog fromCatalog = this.catalogService.getCatalog(fromCatalogId);
		Assert.notNull(fromCatalog,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("fromCatalogId", fromCatalogId));
		CmsCatalog toCatalog = null;
		if (IdUtils.validate(toCatalogId)) {
			toCatalog = this.catalogService.getCatalog(toCatalogId);
			Assert.notNull(toCatalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("toCatalogId", toCatalogId));

			// 目标栏目不能是源栏目的子栏目或自身，且目标栏目不是当前栏目的父级栏目
			boolean isSelfOfChild = toCatalog.getAncestors().startsWith(fromCatalog.getAncestors())
					|| toCatalog.getCatalogId().equals(fromCatalog.getParentId());
			Assert.isFalse(isSelfOfChild, ContentCoreErrorCode.CATALOG_MOVE_TO_SELF_OR_CHILD::exception);
		}
		AsyncTask task = this.catalogService.moveCatalog(fromCatalog, toCatalog);
		return R.ok(task.getTaskId());
	}

	@PutMapping("/sort")
	public R<?> sortCatalog(@RequestBody SortCatalogDTO dto) {
		if (dto.getSort() == 0) {
			return R.fail("排序数值不能为0");
		}
		this.catalogService.sortCatalog(dto.getCatalogId(), dto.getSort());
		return R.ok();
	}
}
