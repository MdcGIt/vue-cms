package com.ruoyi.contentcore.controller;

import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.exception.NotPermissionException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.extend.annotation.XssIgnore;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.staticize.StaticizeService;
import com.ruoyi.common.utils.*;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.CmsTemplate;
import com.ruoyi.contentcore.domain.dto.TemplateAddDTO;
import com.ruoyi.contentcore.domain.dto.TemplateRenameDTO;
import com.ruoyi.contentcore.domain.dto.TemplateUpdateDTO;
import com.ruoyi.contentcore.domain.vo.TemplateListVO;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.fixed.config.TemplateSuffix;
import com.ruoyi.contentcore.perms.ContentCorePriv;
import com.ruoyi.contentcore.perms.SitePermissionType;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.service.ITemplateService;
import com.ruoyi.contentcore.util.CmsPrivUtils;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 模板管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/template")
@RequiredArgsConstructor
public class TemplateController extends BaseRestController {

	private final ITemplateService templateService;

	private final ISiteService siteService;

	private final StaticizeService staticizeService;

	/**
	 * 模板数据集合
	 */
	@Priv(
		type = AdminUserType.TYPE,
		value = { CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
		mode = SaMode.AND
	)
	@GetMapping
	public R<?> getTemplateList(@RequestParam(value = "publishPipeCode", required = false) String publishPipeCode,
								@RequestParam(value = "filename", required = false) String filename) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.templateService.scanTemplates(site);
		Page<CmsTemplate> page = this.templateService.lambdaQuery().eq(CmsTemplate::getSiteId, site.getSiteId())
				.eq(StringUtils.isNotEmpty(publishPipeCode), CmsTemplate::getPublishPipeCode, publishPipeCode)
				.like(StringUtils.isNotEmpty(filename), CmsTemplate::getPath, filename)
				.orderByAsc(CmsTemplate::getPath)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		List<TemplateListVO> list = page.getRecords().stream().map(t -> TemplateListVO.builder()
				.templateId(t.getTemplateId())
				.path(t.getPath())
				.publishPipeCode(t.getPublishPipeCode())
				.siteId(t.getSiteId())
				.filesize(t.getFilesize())
				.filesizeName(FileUtils.byteCountToDisplaySize(t.getFilesize()))
				.modifyTime(DateUtils.epochMilliToLocalDateTime(t.getModifyTime()))
				.build()).toList();
		return this.bindDataTable(list, (int) page.getTotal());
	}

	/**
	 * 获取模板详情
	 */
	@Priv(
		type = AdminUserType.TYPE,
		value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
		mode = SaMode.AND
	)
	@GetMapping("/{templateId}")
	public R<?> getTemplateDetail(@PathVariable("templateId") String templateId) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.templateService.scanTemplates(site);

		CmsTemplate template = this.templateService.getById(templateId);
		Assert.notNull(template, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("templateId", templateId));

		Assert.isTrue(Objects.equals(template.getSiteId(), site.getSiteId()),
				() -> new NotPermissionException(SitePermissionType.SitePrivItem.View.getPermissionKey(site.getSiteId())));
		return R.ok(template);
	}

	/**
	 * 新增模板文件
	 */
	@Priv(
		type = AdminUserType.TYPE,
		value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
		mode = SaMode.AND
	)
	@Log(title = "新增模板", businessType = BusinessType.INSERT)
	@XssIgnore
	@PostMapping
	public R<?> add(@RequestBody @Validated TemplateAddDTO dto) throws IOException {
		Assert.isTrue(validTemplateName(dto.getPath()), ContentCoreErrorCode.INVALID_TEMPLATE_NAME::exception);
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		dto.setSiteId(site.getSiteId());
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.templateService.addTemplate(dto);
		return R.ok();
	}

	/**
	 * 重命名模板文件
	 */
	@Priv(
		type = AdminUserType.TYPE,
		value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
		mode = SaMode.AND
	)
	@Log(title = "重命名模板", businessType = BusinessType.UPDATE)
	@PostMapping("/rename")
	public R<?> rename(@RequestBody @Validated TemplateRenameDTO dto) throws IOException {
		Assert.isTrue(validTemplateName(dto.getPath()), ContentCoreErrorCode.INVALID_TEMPLATE_NAME::exception);
		CmsTemplate template = this.templateService.getById(dto.getTemplateId());
		Assert.notNull(template,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("templateId", dto.getTemplateId()));

		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		Assert.isTrue(Objects.equals(template.getSiteId(), site.getSiteId()),
				() -> new NotPermissionException(SitePermissionType.SitePrivItem.View.getPermissionKey(site.getSiteId())));

		this.templateService.renameTemplate(template, dto.getPath(), dto.getRemark(), StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	private static boolean validTemplateName(String fileName) {
		String suffix = TemplateSuffix.getValue();
		if (StringUtils.isEmpty(fileName) || !fileName.endsWith(suffix)) {
			return false;
		}
		fileName = FileExUtils.normalizePath(fileName);
		String[] split = fileName.substring(0, fileName.indexOf(suffix)).split("/");
		for (String item : split) {
			System.out.println(item);
			System.out.println(Pattern.matches("[a-zA-Z0-9_]+", item));
			if (StringUtils.isEmpty(item) || !Pattern.matches("^[a-zA-Z0-9_]+$", item)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 修改模板文件内容
	 */
	@Priv(
		type = AdminUserType.TYPE,
		value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
		mode = SaMode.AND
	)
	@Log(title = "编辑模板", businessType = BusinessType.UPDATE)
	@XssIgnore
	@PutMapping
	public R<?> save(@RequestBody @Validated TemplateUpdateDTO dto) throws IOException {
		CmsTemplate template = this.templateService.getById(dto.getTemplateId());
		Assert.notNull(template,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("templateId", dto.getTemplateId()));

		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		Assert.isTrue(Objects.equals(template.getSiteId(), site.getSiteId()),
				() -> new NotPermissionException(SitePermissionType.SitePrivItem.View.getPermissionKey(site.getSiteId())));

		dto.setOperator(StpAdminUtil.getLoginUser());
		this.templateService.saveTemplate(template, dto);
		return R.ok();
	}

	/**
	 * 删除模板文件
	 */
	@Priv(
		type = AdminUserType.TYPE,
		value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
		mode = SaMode.AND
	)
	@Log(title = "删除模板", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> delete(@RequestBody @NotEmpty List<Long> templateIds) throws IOException {
		Assert.isTrue(IdUtils.validate(templateIds), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("templateIds"));
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.templateService.deleteTemplates(site, templateIds);
		return R.ok();
	}

	@Priv(
		type = AdminUserType.TYPE,
		value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
		mode = SaMode.AND
	)
	@Log(title = "清理模板缓存", businessType = BusinessType.OTHER)
	@PostMapping("/clearTemplateCache")
	public R<?> clearTemplateCache() {
		this.staticizeService.clearTemplateCache();
		return R.ok();
	}

	@Priv(
		type = AdminUserType.TYPE,
		value = { ContentCorePriv.TemplateView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
		mode = SaMode.AND
	)
	@Log(title = "清理区块缓存", businessType = BusinessType.OTHER)
	@DeleteMapping("/clearIncludeCache")
	public R<?> clearIncludeCache(@RequestBody @NotEmpty List<Long> templateIds) {
		this.templateService.listByIds(templateIds).forEach(template -> {
			CmsSite site = this.siteService.getSite(template.getSiteId());
			String templateKey = SiteUtils.getTemplateKey(site, template.getPublishPipeCode(), template.getPath());
			this.templateService.clearTemplateStaticContentCache(templateKey);
		});
		return R.ok();
	}
}
