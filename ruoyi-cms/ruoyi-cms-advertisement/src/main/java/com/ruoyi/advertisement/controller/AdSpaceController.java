package com.ruoyi.advertisement.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.advertisement.AdSpacePageWidgetType;
import com.ruoyi.advertisement.pojo.vo.AdSpaceVO;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IPageWidget;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.PageWidgetAddDTO;
import com.ruoyi.contentcore.domain.dto.PageWidgetEditDTO;
import com.ruoyi.contentcore.domain.vo.PageWidgetVO;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IPageWidgetService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;

import freemarker.template.TemplateException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 广告位页面部件管理前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/adspace")
public class AdSpaceController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IPageWidgetService pageWidgetService;

	private final AdSpacePageWidgetType pageWidgetType;

	@GetMapping
	public R<?> listAdSpaces(@RequestParam(name = "catalogId", required = false) Long catalogId,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "state", required = false) Integer state) {
		PageRequest pr = getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		LambdaQueryWrapper<CmsPageWidget> q = new LambdaQueryWrapper<CmsPageWidget>()
				.eq(CmsPageWidget::getSiteId, site.getSiteId())
				.eq(catalogId != null && catalogId > 0, CmsPageWidget::getCatalogId, catalogId)
				.like(StringUtils.isNotEmpty(name), CmsPageWidget::getName, name)
				.eq(CmsPageWidget::getType, AdSpacePageWidgetType.ID)
				.eq(state != null && state > -1, CmsPageWidget::getState, state)
				.orderByDesc(CmsPageWidget::getCreateTime);
		Page<CmsPageWidget> page = pageWidgetService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		List<AdSpaceVO> list = new ArrayList<>();
		page.getRecords().forEach(pw -> {
			AdSpaceVO vo = (AdSpaceVO) pageWidgetType.getPageWidgetVO(pw);
			if (pw.getCatalogId() > 0) {
				CmsCatalog catalog = catalogService.getCatalog(pw.getCatalogId());
				vo.setCatalogName(catalog != null ? catalog.getName() : "未知");
			}
			list.add(vo);
		});
		return this.bindDataTable(list, (int) page.getTotal());
	}

	@GetMapping("/{adSpaceId}")
	public R<PageWidgetVO> getAdSpaceInfo(@PathVariable("adSpaceId") Long adSpaceId) {
		CmsPageWidget pageWidget = this.pageWidgetService.getById(adSpaceId);
		if (pageWidget == null) {
			return R.fail("数据未找到：" + adSpaceId);
		}
		AdSpaceVO vo = (AdSpaceVO) pageWidgetType.getPageWidgetVO(pageWidget);
		CmsCatalog catalog = this.catalogService.getCatalog(pageWidget.getCatalogId());
		vo.setCatalogName(catalog != null ? catalog.getName() : "未知");
		return R.ok(vo);
	}

	@PostMapping
	public R<?> addAdSpace(HttpServletRequest request) throws IOException {
		PageWidgetAddDTO dto = JacksonUtils.from(request.getInputStream(), PageWidgetAddDTO.class);
		dto.setType(pageWidgetType.getId());
		
		CmsPageWidget cmsPageWdiget = new CmsPageWidget();
		BeanUtils.copyProperties(dto, cmsPageWdiget);
		IPageWidget pw = pageWidgetType.newInstance();
		pw.setPageWidgetEntity(cmsPageWdiget);
		pw.setOperator(StpAdminUtil.getLoginUser());
		CmsSite site = this.siteService.getCurrentSite(request);
		pw.getPageWidgetEntity().setSiteId(site.getSiteId());
		this.pageWidgetService.addPageWidget(pw);
		return R.ok();
	}

	@PutMapping
	public R<?> editAdSpace(HttpServletRequest request) throws IOException {
		PageWidgetEditDTO dto = JacksonUtils.from(request.getInputStream(), PageWidgetEditDTO.class);
		CmsPageWidget cmsPageWdiget = new CmsPageWidget();
		BeanUtils.copyProperties(dto, cmsPageWdiget);
		IPageWidget pw = pageWidgetType.newInstance();
		pw.setPageWidgetEntity(cmsPageWdiget);
		pw.setOperator(StpAdminUtil.getLoginUser());
		this.pageWidgetService.savePageWidget(pw);
		return R.ok();
	}

	@DeleteMapping
	public R<?> deleteAdspaces(@RequestBody List<Long> adSpaceIds) {
		Assert.notEmpty(adSpaceIds, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("adSpaceIds"));
		this.pageWidgetService.deletePageWidgets(adSpaceIds);
		return R.ok();
	}

	@PostMapping("/publish")
	public R<?> publishPageWdigets(@RequestBody List<Long> adSpaceIds) throws TemplateException, IOException {
		Assert.notEmpty(adSpaceIds, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("adSpaceIds"));
		this.pageWidgetService.publishPageWidgets(adSpaceIds);
		return R.ok();
	}
}
