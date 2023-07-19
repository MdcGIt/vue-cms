package com.ruoyi.contentcore.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.dev33.satoken.annotation.SaMode;
import com.ruoyi.contentcore.util.CmsPrivUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
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
import com.ruoyi.contentcore.core.IPageWidget;
import com.ruoyi.contentcore.core.IPageWidgetType;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.PageWidgetAddDTO;
import com.ruoyi.contentcore.domain.dto.PageWidgetEditDTO;
import com.ruoyi.contentcore.domain.vo.PageWidgetVO;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.perms.ContentCorePriv;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IPageWidgetService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;

import freemarker.template.TemplateException;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * 页面部件控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(
	type = AdminUserType.TYPE,
	value = { ContentCorePriv.ContentView, CmsPrivUtils.PRIV_SITE_VIEW_PLACEHOLDER},
	mode = SaMode.AND
)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/pagewidget")
public class PageWidgetController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IPageWidgetService pageWidgetService;
	
	@GetMapping("/types")
	public R<?> getPageWidgetTypes() {
		List<Map<String, String>> result = this.pageWidgetService.getPageWidgetTypes().stream().map(bt -> {
			return Map.of("id", bt.getId(), "name", I18nUtils.get(bt.getName()));
		}).toList();
		return this.bindDataTable(result);
	}

	@GetMapping
	public R<?> listData(@RequestParam(name = "catalogId", required = false) Long catalogId,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "type", required = false) String type,
			@RequestParam(name = "state", required = false) Integer state) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		LambdaQueryWrapper<CmsPageWidget> q = new LambdaQueryWrapper<CmsPageWidget>()
				.eq(CmsPageWidget::getSiteId, site.getSiteId())
//				.eq(catalogId != null && catalogId > 0, CmsPageWidget::getCatalogId, catalogId)
				.like(StringUtils.isNotEmpty(name), CmsPageWidget::getName, name)
				.eq(StringUtils.isNotEmpty(type), CmsPageWidget::getType, type)
				.eq(state != null && state > -1, CmsPageWidget::getState, state)
				.orderByDesc(CmsPageWidget::getCreateTime);
		Page<CmsPageWidget> page = pageWidgetService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		List<PageWidgetVO> list = new ArrayList<>();
		page.getRecords().forEach(pw -> {
			PageWidgetVO vo = PageWidgetVO.newInstance(pw);
			if (pw.getCatalogId() > 0) {
				CmsCatalog catalog = catalogService.getCatalog(pw.getCatalogId());
				vo.setCatalogName(catalog.getName());
			}
			IPageWidgetType pageWidgetType = this.pageWidgetService.getPageWidgetType(vo.getType());
			vo.setRoute(pageWidgetType.getRoute());
			list.add(vo);
		});
		return this.bindDataTable(list, (int) page.getTotal());
	}

	@GetMapping("/{pageWidgetId}")
	public R<PageWidgetVO> getInfo(@PathVariable("pageWidgetId") Long pageWidgetId) {
		CmsPageWidget pageWidget = this.pageWidgetService.getById(pageWidgetId);
		Assert.notNull(pageWidget, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("pageWidgetId", pageWidgetId));

		IPageWidgetType pwt = this.pageWidgetService.getPageWidgetType(pageWidget.getType());
		PageWidgetVO vo = pwt.getPageWidgetVO(pageWidget);

		if (pageWidget.getCatalogId() > 0) {
			CmsCatalog catalog = this.catalogService.getCatalog(pageWidget.getCatalogId());
			vo.setCatalogName(catalog.getName());
		}
		return R.ok(vo);
	}

	@Log(title = "新增页面组件", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addPageWidget(@RequestBody @Validated PageWidgetAddDTO dto)
			throws IOException {
		IPageWidgetType pwt = this.pageWidgetService.getPageWidgetType(dto.getType());
		Assert.notNull(pwt, () -> ContentCoreErrorCode.UNSUPPORT_PAGE_WIDGET_TYPE.exception(dto.getType()));

		CmsPageWidget pageWidget= new CmsPageWidget();
		BeanUtils.copyProperties(dto, pageWidget);
		
		IPageWidget pw = pwt.newInstance();
		pw.setPageWidgetEntity(pageWidget);
		pw.setOperator(StpAdminUtil.getLoginUser());
		if (pageWidget.getCatalogId() != null && pageWidget.getCatalogId() > 0) {
			CmsCatalog catalog = this.catalogService.getCatalog(pageWidget.getCatalogId());
			pageWidget.setSiteId(catalog.getSiteId());
			pageWidget.setCatalogAncestors(catalog.getAncestors());
		} else {
			CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
			pw.getPageWidgetEntity().setSiteId(site.getSiteId());
		}
		this.pageWidgetService.addPageWidget(pw);
		return R.ok();
	}

	@Log(title = "编辑页面组件", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> editPageWidget(@RequestBody @Validated PageWidgetEditDTO dto)
			throws IOException {
		CmsPageWidget pageWidget = this.pageWidgetService.getById(dto.getPageWidgetId());
		Assert.notNull(dto.getPageWidgetId(), () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("pageWidgetId", dto.getPageWidgetId()));
		
		BeanUtils.copyProperties(dto, pageWidget);
		pageWidget.setContent(dto.getContentStr());
		
		IPageWidgetType pwt = this.pageWidgetService.getPageWidgetType(pageWidget.getType());
		IPageWidget pw = pwt.newInstance();
		pw.setPageWidgetEntity(pageWidget);
		pw.setOperator(StpAdminUtil.getLoginUser());
		this.pageWidgetService.savePageWidget(pw);
		return R.ok();
	}

	@Log(title = "删除页面组件", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> deletePageWidgets(@RequestBody @NotEmpty List<Long> pageWidgetIds) {
		this.pageWidgetService.deletePageWidgets(pageWidgetIds);
		return R.ok();
	}

	@Log(title = "发布页面组件", businessType = BusinessType.OTHER)
	@PostMapping("/publish")
	public R<?> publishPageWdigets(@RequestBody @NotEmpty List<Long> pageWidgetIds) throws TemplateException, IOException {
		this.pageWidgetService.publishPageWidgets(pageWidgetIds);
		return R.ok();
	}
}
