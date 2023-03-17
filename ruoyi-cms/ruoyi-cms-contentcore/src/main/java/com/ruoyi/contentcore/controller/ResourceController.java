package com.ruoyi.contentcore.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.impl.InternalDataType_Resource;
import com.ruoyi.contentcore.domain.CmsResource;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.ImageCropDTO;
import com.ruoyi.contentcore.domain.dto.ResourceUploadDTO;
import com.ruoyi.contentcore.service.IResourceService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@SaAdminCheckLogin
@RestController
@RequestMapping("/cms/resource")
@RequiredArgsConstructor
public class ResourceController extends BaseRestController {

	private final ISiteService siteService;

	private final IResourceService resourceService;

	@GetMapping("/list")
	public R<?> listData(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "resourceType", required = false) String resourceType,
			@RequestParam(value = "beginTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginTime,
			@RequestParam(value = "endTime", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		LambdaQueryWrapper<CmsResource> q = new LambdaQueryWrapper<CmsResource>()
				.eq(CmsResource::getSiteId, site.getSiteId())
				.like(StringUtils.isNotEmpty(name), CmsResource::getFileName, name)
				.eq(StringUtils.isNotEmpty(resourceType), CmsResource::getResourceType, resourceType)
				.ge(beginTime != null, CmsResource::getCreateTime, beginTime)
				.le(endTime != null, CmsResource::getCreateTime, endTime).orderByDesc(CmsResource::getResourceId);
		Page<CmsResource> page = resourceService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		if (page.getRecords().size() > 0) {
			page.getRecords().forEach(r -> {
				if (r.getPath().startsWith("http://") || r.getPath().startsWith("https://")) {
					r.setSrc(r.getPath());
				} else {
					String resourceLink = this.resourceService.getResourceLink(r, true);
					r.setSrc(resourceLink);
				}
				r.setInternalUrl(InternalDataType_Resource.getInternalUrl(r));
			});
		}
		return bindDataTable(page);
	}

	@GetMapping("/info/{resourceId}")
	public R<CmsResource> getInfo(@PathVariable("resourceId") Long resourceId) {
		CmsResource resource = this.resourceService.getById(resourceId);
		if (resource == null) {
			return R.fail("资源ID错误：" + resourceId);
		}
		return R.ok(resource);
	}

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

	@DeleteMapping("/{resourceIds}")
	public R<String> delResources(@PathVariable("resourceIds") Long[] resourceIds) {
		Assert.notEmpty(resourceIds, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("resourceIds"));
		return this.resourceService.deleteResource(Arrays.asList(resourceIds));
	}

	@PostMapping("/upload")
	public R<CmsResource> uploadFile(@RequestParam("file") MultipartFile multipartFile) throws Exception {
		Assert.notNull(multipartFile, () -> CommonErrorCode.NOT_EMPTY.exception("file"));

		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		ResourceUploadDTO dto = ResourceUploadDTO.builder().site(site).file(multipartFile).build();
		dto.setOperator(StpAdminUtil.getLoginUser());
		CmsResource resource = this.resourceService.addResource(dto);
		resource.setSrc(SiteUtils.getResourcePrefix(site) + resource.getPath());
		return R.ok(resource);
	}

	@GetMapping("/downlad/{resourceId}")
	public void downloadResourceFile(@PathVariable Long resourceId, HttpServletResponse response) {
		CmsResource resource = this.resourceService.getById(resourceId);
		Assert.notNull(resource, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("resourceId", resourceId));
		this.resourceService.downloadResource(resource, response);
	}

	@PostMapping("/image/cut")
	public R<?> cutImage(@RequestBody ImageCropDTO imageCutDTO) {

		return R.ok();
	}
}
