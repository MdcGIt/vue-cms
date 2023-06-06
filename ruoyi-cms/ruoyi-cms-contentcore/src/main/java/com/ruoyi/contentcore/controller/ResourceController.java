package com.ruoyi.contentcore.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IResourceType;
import com.ruoyi.contentcore.core.impl.InternalDataType_Resource;
import com.ruoyi.contentcore.domain.CmsResource;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.ImageCropDTO;
import com.ruoyi.contentcore.domain.dto.ResourceUploadDTO;
import com.ruoyi.contentcore.perms.ContentCorePriv;
import com.ruoyi.contentcore.service.IResourceService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.validator.LongId;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * 素材库管理控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE, value = ContentCorePriv.ResourceView)
@RestController
@RequestMapping("/cms/resource")
@RequiredArgsConstructor
public class ResourceController extends BaseRestController {

	private final ISiteService siteService;

	private final IResourceService resourceService;

	@GetMapping("/types")
	public R<?> getResourceTypes() {
		List<Map<String, String>> list = ContentCoreUtils.getResourceTypes().stream()
				.map(rt -> Map.of(
						"id", rt.getId(),
						"name", I18nUtils.get(rt.getName()),
						"accepts", StringUtils.join(rt.getUsableSuffix(), ","))
				).toList();
		return R.ok(list);
	}

	@GetMapping
	public R<?> listData(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "resourceType", required = false) String resourceType,
			@RequestParam(value = "owner", required = false, defaultValue = "false") boolean owner,
			@RequestParam(value = "beginTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginTime,
			@RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		LambdaQueryWrapper<CmsResource> q = new LambdaQueryWrapper<CmsResource>()
				.eq(CmsResource::getSiteId, site.getSiteId())
				.like(StringUtils.isNotEmpty(name), CmsResource::getFileName, name)
				.eq(StringUtils.isNotEmpty(resourceType), CmsResource::getResourceType, resourceType)
				.eq(owner, CmsResource::getCreateBy, StpAdminUtil.getLoginUser().getUsername())
				.ge(beginTime != null, CmsResource::getCreateTime, beginTime)
				.le(endTime != null, CmsResource::getCreateTime, endTime).orderByDesc(CmsResource::getResourceId);
		Page<CmsResource> page = resourceService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		if (page.getRecords().size() > 0) {
			page.getRecords().forEach(r -> {
				IResourceType rt = ContentCoreUtils.getResourceType(r.getResourceType());
				r.setResourceTypeName(I18nUtils.get(rt.getName()));
				if (r.getPath().startsWith("http://") || r.getPath().startsWith("https://")) {
					r.setSrc(r.getPath());
				} else {
					String resourceLink = this.resourceService.getResourceLink(r, true);
					r.setSrc(resourceLink);
				}
				r.setInternalUrl(InternalDataType_Resource.getInternalUrl(r));
				r.setFileSizeName(FileUtils.byteCountToDisplaySize(r.getFileSize()));
			});
		}
		return bindDataTable(page);
	}

	@GetMapping("/{resourceId}")
	public R<CmsResource> getInfo(@PathVariable("resourceId") @LongId Long resourceId) {
		CmsResource resource = this.resourceService.getById(resourceId);
		if (resource == null) {
			return R.fail("资源ID错误：" + resourceId);
		}
		return R.ok(resource);
	}

	@Log(title = "新增素材", businessType = BusinessType.INSERT)
	@PostMapping
	public R<CmsResource> addResource(@RequestParam("file") MultipartFile resourceFile, String name, String remark) {
		Assert.isFalse(resourceFile.isEmpty(), () -> CommonErrorCode.NOT_EMPTY.exception("file"));
		try {
			CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
			ResourceUploadDTO dto = ResourceUploadDTO.builder().site(site).file(resourceFile).name(name).remark(remark)
					.build();
			dto.setOperator(StpAdminUtil.getLoginUser());
			CmsResource resource = this.resourceService.addResource(dto);
			return R.ok(resource);
		} catch (IOException e1) {
			throw CommonErrorCode.SYSTEM_ERROR.exception(e1.getMessage());
		}
	}

	@Log(title = "删除素材", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<String> delResources(@RequestBody @NotEmpty List<Long> resourceIds) {
		Assert.notEmpty(resourceIds, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("resourceIds"));
		this.resourceService.deleteResource(resourceIds);
		return R.ok();
	}

	@Log(title = "上传素材", businessType = BusinessType.INSERT)
	@PostMapping("/upload")
	public R<CmsResource> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws Exception {
		Assert.notNull(multipartFile, () -> CommonErrorCode.NOT_EMPTY.exception("file"));

		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		ResourceUploadDTO dto = ResourceUploadDTO.builder().site(site).file(multipartFile).build();
		dto.setOperator(StpAdminUtil.getLoginUser());
		CmsResource resource = this.resourceService.addResource(dto);
		resource.setSrc(InternalUrlUtils.getActualPreviewUrl(resource.getInternalUrl()));
		return R.ok(resource);
	}

	@GetMapping("/downlad/{resourceId}")
	public void downloadResourceFile(@PathVariable @LongId Long resourceId, HttpServletResponse response) {
		CmsResource resource = this.resourceService.getById(resourceId);
		Assert.notNull(resource, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("resourceId", resourceId));
		this.resourceService.downloadResource(resource, response);
	}

	@Log(title = "图片裁剪", businessType = BusinessType.UPDATE)
	@PostMapping("/image/cut")
	public R<?> cutImage(@RequestBody @Validated ImageCropDTO imageCutDTO) {
		// TODO 
		return R.fail("TODO");
	}
}
