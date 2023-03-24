package com.ruoyi.contentcore.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.extend.annotation.XssIgnore;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.staticize.StaticizeService;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.CmsTemplate;
import com.ruoyi.contentcore.domain.dto.TemplateAddDTO;
import com.ruoyi.contentcore.domain.dto.TemplateRenameDTO;
import com.ruoyi.contentcore.domain.dto.TemplateUpdateDTO;
import com.ruoyi.contentcore.domain.vo.TemplateListVO;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.service.ITemplateService;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;

import lombok.RequiredArgsConstructor;

/**
 * 模板管理
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
@RestController
@RequestMapping("/cms/template")
@RequiredArgsConstructor
public class TemplateController extends BaseRestController {

	private final ITemplateService templateService;

	private final ISiteService siteService;

	private final StaticizeService staticizeService;

	/**
	 * 模板数据集合
	 * 
	 * @param publishPipeCode 发布通道编码
	 * @param filename        文件名
	 * @return
	 */
	@GetMapping
	public R<?> getTemplateList(@RequestParam(value = "publishPipeCode", required = false) String publishPipeCode,
			@RequestParam(value = "filename", required = false) String filename) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.templateService.scanTemplates(site);
		Page<CmsTemplate> page = this.templateService.lambdaQuery().eq(CmsTemplate::getSiteId, site.getSiteId())
				.eq(StringUtils.isNotEmpty(publishPipeCode), CmsTemplate::getPublishPipeCode, publishPipeCode)
				.like(StringUtils.isNotEmpty(filename), CmsTemplate::getPath, filename)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		List<TemplateListVO> list = page.getRecords().stream().map(t -> {
			return TemplateListVO.builder().templateId(t.getTemplateId()).path(t.getPath())
					.publishPipeCode(t.getPublishPipeCode()).siteId(t.getSiteId()).filesize(t.getFilesize())
					.filesizeName(FileUtils.byteCountToDisplaySize(t.getFilesize())).build();
		}).toList();
		return this.bindDataTable(list, (int) page.getTotal());
	}

	/**
	 * 获取模板详情
	 * 
	 * @param publishPipeCode 发布通道编码
	 * @param filename        文件名
	 * @return
	 * @throws IOException
	 */
	@GetMapping("/{templateId}")
	public R<?> getTemplateDetail(@PathVariable("templateId") String templateId) throws IOException {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.templateService.scanTemplates(site);

		CmsTemplate template = this.templateService.getById(templateId);
		Assert.notNull(template, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("templateId", templateId));

		return R.ok(template);
	}

	/**
	 * 新增模板文件
	 * 
	 * @param cmsTemplateDto
	 * @return
	 * @throws IOException
	 */
	@XssIgnore
	@PostMapping
	public R<?> add(@RequestBody TemplateAddDTO dto) throws IOException {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		dto.setSiteId(site.getSiteId());
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.templateService.addTemplate(dto);
		return R.ok();
	}

	/**
	 * 重命名模板文件
	 * 
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	@PostMapping("/rename")
	public R<?> rename(@RequestBody TemplateRenameDTO dto) throws IOException {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.templateService.renameTemplate(dto);
		return R.ok();
	}

	/**
	 * 修改模板文件内容
	 * 
	 * @param cmsTemplateDto
	 * @return
	 * @throws IOException
	 */
	@XssIgnore
	@PutMapping
	public R<?> save(@RequestBody TemplateUpdateDTO dto) throws IOException {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.templateService.saveTemplate(dto);
		return R.ok();
	}

	/**
	 * 删除模板文件
	 * 
	 * @param cmsTemplateDtoList
	 * @return
	 * @throws IOException
	 */
	@DeleteMapping
	public R<?> delete(@RequestBody List<Long> templateIds) throws IOException {
		Assert.notEmpty(templateIds, () -> CommonErrorCode.NOT_EMPTY.exception());
		this.templateService.deleteTemplates(templateIds);
		return R.ok();
	}

	@PostMapping("/clearTemplateCache")
	public R<?> clearTemplateCache() {
		this.staticizeService.clearTemplateCache();
		return R.ok();
	}
}