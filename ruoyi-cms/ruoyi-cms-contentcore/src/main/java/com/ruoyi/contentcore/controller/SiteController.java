package com.ruoyi.contentcore.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.MapUtils;
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
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.core.IProperty.UseType;
import com.ruoyi.contentcore.core.IPublishPipeProp.PublishPipePropUseType;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.PublishPipeProp;
import com.ruoyi.contentcore.domain.dto.PublishSiteDTO;
import com.ruoyi.contentcore.domain.dto.SiteDTO;
import com.ruoyi.contentcore.domain.dto.SiteDefaultTemplateDTO;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.IPublishService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.system.security.SaAdminCheckLogin;
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
@SaAdminCheckLogin
@RestController
@RequestMapping("/cms/site")
@RequiredArgsConstructor
public class SiteController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IPublishPipeService publishPipeService;

	private final IPublishService publishService;

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
		page.getRecords().forEach(site -> {
			if (StringUtils.isNotEmpty(site.getLogo())) {
				site.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(site.getLogo()));
			}
		});
		return this.bindDataTable(page);
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
		if (site == null) {
			return R.fail("站点数据未找到：" + siteId);
		}
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
	@PutMapping
	public R<?> editSave(@RequestBody SiteDTO dto) throws IOException {
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
	@DeleteMapping("/{siteId}")
	public R<String> remove(@PathVariable("siteId") Long siteId) throws IOException {
		this.siteService.deleteSite(siteId);
		return R.ok();
	}

	/**
	 * 发布站点
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 * @throws TemplateException
	 */
	@PostMapping("/publish")
	public R<String> publishAll(@RequestBody PublishSiteDTO dto) throws IOException, TemplateException {
		CmsSite site = siteService.getById(dto.getSiteId());
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", dto.getSiteId()));

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

		Map<String, Object> configProps = site.getConfigProps();
		if (configProps == null) {
			configProps = new HashMap<>();
		}
		List<IProperty> props = ConfigPropertyUtils.getConfigPropertiesByUseType(UseType.Site);
		for (IProperty prop : props) {
			String value = MapUtils.getString(configProps, prop.getId(), prop.defaultValue());
			if (Objects.nonNull(prop.valueClass())) {
				configProps.put(prop.getId(), JacksonUtils.from(value, prop.valueClass()));
			} else {
				configProps.put(prop.getId(), value);
			}
		}
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
	@PostMapping("/extends/{siteId}")
	public R<?> saveSiteExtends(@PathVariable("siteId") Long siteId, @RequestBody Map<String, Object> configs) {
		this.siteService.saveSiteExtend(siteId, configs, StpAdminUtil.getLoginUser().getUsername());
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
		CmsSite site = siteService.getById(siteId);
		if (site == null) {
			return R.fail("数据ID错误：" + siteId);
		}
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
	@PostMapping("/default_template")
	public R<?> saveDefaultTemplates(@RequestBody SiteDefaultTemplateDTO dto) {
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
	@PostMapping("/apply_default_template")
	public R<?> applyDefaultTemplateToCatalog(@RequestBody SiteDefaultTemplateDTO dto) {
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
	@PostMapping("/upload_watermarkimage")
	public R<?> uploadFile(@RequestParam("siteId") @Min(1) Long siteId,
			@RequestParam("file") @NotNull MultipartFile multipartFile) throws Exception {
		try {
			CmsSite site = this.siteService.getSite(siteId);
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