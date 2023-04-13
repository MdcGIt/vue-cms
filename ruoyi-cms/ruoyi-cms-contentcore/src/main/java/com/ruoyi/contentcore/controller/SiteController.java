package com.ruoyi.contentcore.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.io.Files;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.core.IProperty.UseType;
import com.ruoyi.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.PublishPipeProp;
import com.ruoyi.contentcore.domain.dto.PublishSiteDTO;
import com.ruoyi.contentcore.domain.dto.SiteDTO;
import com.ruoyi.contentcore.domain.dto.SiteDefaultTemplateDTO;
import com.ruoyi.contentcore.perms.ContentCorePriv;
import com.ruoyi.contentcore.perms.SitePermissionType.SitePrivItem;
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

import freemarker.template.TemplateException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

/**
 * 站点管理
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Priv(type = AdminUserType.TYPE, value = ContentCorePriv.SiteView)
@RestController
@RequestMapping("/cms/site")
@RequiredArgsConstructor
public class SiteController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IPublishPipeService publishPipeService;

	private final IPublishService publishService;

	private final AsyncTaskManager asyncTaskManager;

	/**
	 * 获取当前站点数据
	 * 
	 * @apiNote 读取request.header['CurrentSite']中的siteId，如果无header或无站点则取数据库第一条站点数据
	 * @return
	 */
	@GetMapping("/getCurrentSite")
	public R<Map<String, Object>> getCurrentSite() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		return R.ok(Map.of("siteId", site.getSiteId(), "siteName", site.getName()));
	}

	/**
	 * 设置当前站点
	 * 
	 * @param siteId 站点ID
	 * @return
	 */
	@Log(title = "切换站点", businessType = BusinessType.UPDATE)
	@PostMapping("/setCurrentSite/{siteId}")
	public R<Map<String, Object>> setCurrentSite(@PathVariable("siteId") @Min(1) Long siteId) {
		CmsSite site = this.siteService.getSite(siteId);
		return R.ok(Map.of("siteId", site.getSiteId(), "siteName", site.getName()));
	}

	/**
	 * 查询站点数据列表
	 * 
	 * @param siteName 站点名称
	 * @return
	 */
	@GetMapping("/list")
	public R<?> list(@RequestParam(value = "siteName", required = false) String siteName) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<CmsSite> q = new LambdaQueryWrapper<CmsSite>().like(StringUtils.isNotEmpty(siteName),
				CmsSite::getName, siteName);
		Page<CmsSite> page = siteService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		List<CmsSite> list = page.getRecords().stream()
				.filter(site -> CmsPrivUtils.hasSitePermission(site.getSiteId(), SitePrivItem.View, loginUser))
				.toList();
		list.forEach(site -> {
			if (StringUtils.isNotEmpty(site.getLogo())) {
				site.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(site.getLogo()));
			}
		});
		return this.bindDataTable(list, page.getTotal());
	}

	@GetMapping("/options")
	public R<?> getSiteOptions() {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		List<Map<String, Object>> list = this.siteService.lambdaQuery().select(CmsSite::getSiteId, CmsSite::getName)
				.list().stream()
				.filter(site -> CmsPrivUtils.hasSitePermission(site.getSiteId(), SitePrivItem.View, loginUser))
				.map(site -> {
					Map<String, Object> map = new HashMap<>();
					map.put("id", site.getSiteId());
					map.put("name", site.getName());
					return map;
				}).toList();
		return this.bindDataTable(list);
	}

	/**
	 * 获取站点详情
	 * 
	 * @param siteId 站点ID
	 * @return
	 */
	@GetMapping(value = "/{siteId}")
	public R<?> getInfo(@PathVariable Long siteId) {
		CmsSite site = siteService.getById(siteId);
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
		CmsPrivUtils.checkSitePermission(siteId, SitePrivItem.View, StpAdminUtil.getLoginUser());

		if (StringUtils.isNotEmpty(site.getLogo())) {
			site.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(site.getLogo()));
		}
		SiteDTO dto = SiteDTO.newInstance(site);
		// 发布通道数据
		List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(site.getSiteId(),
				PublishPipePropUseType.Site, site.getPublishPipeProps());
		dto.setPublishPipeDatas(publishPipeProps);
		return R.ok(dto);
	}

	/**
	 * 新增站点数据
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@Log(title = "新增站点", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addSave(@RequestBody SiteDTO dto) throws IOException {
		dto.setOperator(StpAdminUtil.getLoginUser());
		CmsSite site = this.siteService.addSite(dto);
		return R.ok(site);
	}

	/**
	 * 修改站点数据
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@Log(title = "编辑站点", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> editSave(@RequestBody SiteDTO dto) throws IOException {
		CmsPrivUtils.checkSitePermission(dto.getSiteId(), SitePrivItem.Edit, StpAdminUtil.getLoginUser());
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.siteService.saveSite(dto);
		return R.ok();
	}

	/**
	 * 删除站点数据
	 * 
	 * @param siteId 站点ID
	 * @return
	 * @throws IOException
	 */
	@Log(title = "删除站点", businessType = BusinessType.DELETE)
	@DeleteMapping("/{siteId}")
	public R<String> remove(@PathVariable("siteId") Long siteId) throws IOException {
		CmsSite site = siteService.getById(siteId);
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
		CmsPrivUtils.checkSitePermission(siteId, SitePrivItem.Delete, StpAdminUtil.getLoginUser());

		AsyncTask task = new AsyncTask() {

			@Override
			public void run0() throws Exception {
				siteService.deleteSite(siteId);
			}
		};
		task.setTaskId("DeleteSite_" + siteId);
		this.asyncTaskManager.execute(task);
		return R.ok(task.getTaskId());
	}

	/**
	 * 发布站点
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	@Log(title = "发布站点", businessType = BusinessType.OTHER)
	@PostMapping("/publish")
	public R<String> publishAll(@RequestBody PublishSiteDTO dto) throws IOException, TemplateException {
		CmsSite site = siteService.getById(dto.getSiteId());
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));
		CmsPrivUtils.checkSitePermission(site.getSiteId(), SitePrivItem.Publish, StpAdminUtil.getLoginUser());

		if (!dto.isPublishIndex()) {
			AsyncTask task = publishService.publishAll(site, dto.getContentStatus());
			return R.ok(task.getTaskId());
		}
		publishService.publishSiteIndex(site);
		return R.ok();
	}

	/**
	 * 获取站点扩展配置数据
	 * 
	 * @param siteId 站点ID
	 * @return
	 */
	@GetMapping("/extends")
	public R<?> getSiteExtends(@RequestParam("siteId") Long siteId) {
		CmsSite site = this.siteService.getSite(siteId);
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
		CmsPrivUtils.checkSitePermission(site.getSiteId(), SitePrivItem.View, StpAdminUtil.getLoginUser());

		Map<String, Object> configProps = ConfigPropertyUtils.paseConfigProps(site.getConfigProps(), UseType.Site);
		configProps.put("PreviewPrefix", SiteUtils.getResourcePrefix(site));
		return R.ok(configProps);
	}

	/**
	 * 保存站点扩展配置数据
	 * 
	 * @param siteId  站点ID
	 * @param configs 扩展配置数据
	 * @return
	 */
	@Log(title = "站点扩展", businessType = BusinessType.UPDATE, isSaveRequestData = false)
	@PostMapping("/extends/{siteId}")
	public R<?> saveSiteExtends(@PathVariable("siteId") Long siteId, @RequestBody Map<String, String> configs) {
		CmsSite site = this.siteService.getSite(siteId);
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
		CmsPrivUtils.checkSitePermission(site.getSiteId(), SitePrivItem.Edit, StpAdminUtil.getLoginUser());

		this.siteService.saveSiteExtend(site, configs, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	/**
	 * 获取站点默认模板配置
	 * 
	 * @param siteId 站点ID
	 * @return
	 */
	@GetMapping("/default_template")
	public R<?> getDefaultTemplates(@RequestParam("siteId") Long siteId) {
		CmsSite site = this.siteService.getSite(siteId);
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
		CmsPrivUtils.checkSitePermission(site.getSiteId(), SitePrivItem.Edit, StpAdminUtil.getLoginUser());

		SiteDefaultTemplateDTO dto = new SiteDefaultTemplateDTO();
		dto.setSiteId(siteId);
		// 发布通道数据
		List<PublishPipeProp> publishPipeProps = this.publishPipeService.getPublishPipeProps(site.getSiteId(),
				PublishPipePropUseType.Site, site.getPublishPipeProps());
		dto.setPublishPipeProps(publishPipeProps);
		return R.ok(dto);
	}

	/**
	 * 保存站点默认模板配置
	 * 
	 * @param dto
	 * @return
	 */
	@Log(title = "默认模板", businessType = BusinessType.UPDATE)
	@PostMapping("/default_template")
	public R<?> saveDefaultTemplates(@RequestBody SiteDefaultTemplateDTO dto) {
		CmsSite site = this.siteService.getSite(dto.getSiteId());
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));
		CmsPrivUtils.checkSitePermission(site.getSiteId(), SitePrivItem.Edit, StpAdminUtil.getLoginUser());

		dto.setOperator(StpAdminUtil.getLoginUser());
		this.siteService.saveSiteDefaultTemplate(dto);
		return R.ok();
	}

	/**
	 * 应用站点默认模板配置到指定栏目
	 * 
	 * @param dto
	 * @return
	 */
	@Log(title = "应用默认模板", businessType = BusinessType.UPDATE)
	@PostMapping("/apply_default_template")
	public R<?> applyDefaultTemplateToCatalog(@RequestBody SiteDefaultTemplateDTO dto) {
		CmsSite site = this.siteService.getSite(dto.getSiteId());
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));
		CmsPrivUtils.checkSitePermission(site.getSiteId(), SitePrivItem.Edit, StpAdminUtil.getLoginUser());

		dto.setOperator(StpAdminUtil.getLoginUser());
		this.catalogService.applySiteDefaultTemplateToCatalog(dto);
		return R.ok();
	}

	/**
	 * 上传水印图片
	 * 
	 * @param siteId        站点ID
	 * @param multipartFile 上传文件
	 * @return
	 * @throws Exception
	 */
	@Log(title = "上传水印图", businessType = BusinessType.UPDATE)
	@PostMapping("/upload_watermarkimage")
	public R<?> uploadFile(@RequestParam("siteId") @Min(1) Long siteId,
			@RequestParam("file") @NotNull MultipartFile multipartFile) throws Exception {
		try {
			CmsSite site = this.siteService.getSite(siteId);
			Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
			CmsPrivUtils.checkSitePermission(site.getSiteId(), SitePrivItem.Edit, StpAdminUtil.getLoginUser());

			String dir = SiteUtils.getSiteResourceRoot(site.getPath());
			String suffix = FileExUtils.getExtension(multipartFile.getOriginalFilename());
			String path = "watermaker" + StringUtils.DOT + suffix;
			File file = new File(dir + path);
			Files.write(multipartFile.getBytes(), file);
			String src = SiteUtils.getResourcePrefix(site) + path;
			return R.ok(Map.of("path", path, "src", src));
		} catch (Exception e) {
			return R.fail(e.getMessage());
		}
	}
}