package com.ruoyi.contentcore.controller;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.extend.annotation.XssIgnore;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.core.impl.InternalDataType_Content;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.*;
import com.ruoyi.contentcore.domain.vo.ContentVO;
import com.ruoyi.contentcore.domain.vo.ListContentVO;
import com.ruoyi.contentcore.fixed.dict.ContentAttribute;
import com.ruoyi.contentcore.listener.event.AfterContentEditorInitEvent;
import com.ruoyi.contentcore.perms.CatalogPermissionType.CatalogPrivItem;
import com.ruoyi.contentcore.perms.ContentCorePriv;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.IPublishService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.user.preference.IncludeChildContentPreference;
import com.ruoyi.contentcore.user.preference.ShowContentSubTitlePreference;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.system.permission.PermissionUtils;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.validator.LongId;
import freemarker.template.TemplateException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 内容管理控制器
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE, value = ContentCorePriv.ContentView)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/content")
public class ContentController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final IContentService contentService;

	private final IPublishService publishService;

	private final ApplicationContext applicationContext;

	/**
	 * 内容列表
	 */
	@DS("slave")
	@GetMapping("/list")
	public R<?> listData(@RequestParam(name = "catalogId", required = false) Long catalogId,
			@RequestParam(name = "title", required = false, defaultValue = "") String title,
		 	@RequestParam(name = "contentType", required = false, defaultValue = "") String contentType,
			@RequestParam(name = "status", required = false) String status) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		if (!IdUtils.validate(catalogId)
				|| !loginUser.hasPermission(CatalogPrivItem.View.getPermissionKey(catalogId))) {
			return this.bindDataTable(List.of());
		}
		PageRequest pr = getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		boolean includeChild = IncludeChildContentPreference.getValue(StpAdminUtil.getLoginUser());

		LambdaQueryChainWrapper<CmsContent> q = this.contentService.lambdaQuery()
				.eq(CmsContent::getSiteId, site.getSiteId())
				.eq(StringUtils.isNotEmpty(contentType), CmsContent::getContentType, contentType)
				.like(StringUtils.isNotEmpty(title), CmsContent::getTitle, title)
				.eq(StringUtils.isNotEmpty(status), CmsContent::getStatus, status);
		if (includeChild) {
			CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
			q.like(CmsContent::getCatalogAncestors, catalog.getAncestors());
		} else {
			q.eq(CmsContent::getCatalogId, catalogId);
		}
		if (pr.getSort().isSorted()) {
			pr.getSort().forEach(order -> {
				SFunction<CmsContent, ?> sfunc = CmsContent.getSFunction(order.getProperty());
				if (Objects.nonNull(sfunc)) {
					q.orderBy(true, order.getDirection() == Direction.ASC, sfunc);
				}
			});
		} else {
			q.orderByDesc(CmsContent::getTopFlag).orderByDesc(CmsContent::getSortFlag);
		}
		Page<CmsContent> page = q.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		List<ListContentVO> list = new ArrayList<>();
		page.getRecords().forEach(content -> {
			ListContentVO vo = new ListContentVO();
			BeanUtils.copyProperties(content, vo);
			vo.setLogoSrc(InternalUrlUtils.getActualPreviewUrl(content.getLogo()));
			vo.setAttributes(ContentAttribute.convertStr(content.getAttributes()));
			vo.setInternalUrl(InternalUrlUtils.getInternalUrl(InternalDataType_Content.ID, content.getContentId()));
			list.add(vo);
		});
		return this.bindDataTable(list, (int) page.getTotal());
	}

	/**
	 * 内容编辑数据初始化
	 */
	@Priv(type = AdminUserType.TYPE, value = "Catalog:View:${#catalogId}")
	@GetMapping("/init/{catalogId}/{contentType}/{contentId}")
	public R<ContentVO> initContentEditor(@PathVariable("catalogId") @LongId Long catalogId,
			@PathVariable("contentType") String contentType, @PathVariable("contentId") Long contentId) {
		IContentType ct = ContentCoreUtils.getContentType(contentType);
		// 获取初始化数据
		ContentVO vo = ct.initEditor(catalogId, contentId);
		vo.setShowSubTitle(ShowContentSubTitlePreference.getValue(StpAdminUtil.getLoginUser()));
		// 事件扩展
		this.applicationContext.publishEvent(new AfterContentEditorInitEvent(this, vo));
		return R.ok(vo);
	}

	@Log(title = "新增内容", businessType = BusinessType.INSERT)
	@XssIgnore
	@PostMapping
	public R<?> addContent(@RequestParam("contentType") String contentType, HttpServletRequest request)
			throws IOException {
		IContentType ct = ContentCoreUtils.getContentType(contentType);

		IContent<?> content = ct.readRequest(request);
		content.setOperator(StpAdminUtil.getLoginUser());
		PermissionUtils.checkPermission(CatalogPrivItem.AddContent.getPermissionKey(content.getCatalogId()),
				content.getOperator());

		AsyncTask task = this.contentService.addContent(content);
		return R.ok(Map.of("taskId", task.getTaskId()));
	}

	@Log(title = "编辑内容", businessType = BusinessType.UPDATE)
	@XssIgnore
	@PutMapping
	public R<?> saveContent(@RequestParam("contentType") String contentType, HttpServletRequest request)
			throws IOException {
		IContentType ct = ContentCoreUtils.getContentType(contentType);

		IContent<?> content = ct.readRequest(request);
		content.setOperator(StpAdminUtil.getLoginUser());
		PermissionUtils.checkPermission(CatalogPrivItem.EditContent.getPermissionKey(content.getCatalogId()),
				content.getOperator());

		AsyncTask task = this.contentService.saveContent(content);
		return R.ok(Map.of("taskId", task.getTaskId()));
	}

	@Log(title = "删除内容", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> deleteContent(@RequestBody @NotEmpty List<Long> contentIds) {
		this.contentService.deleteContents(contentIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	/**
	 * 发布内容
	 */
	@Log(title = "发布内容", businessType = BusinessType.OTHER)
	@PostMapping("/publish")
	public R<String> publish(@RequestBody @Validated PublishContentDTO publishContentDTO) throws TemplateException, IOException {
		this.publishService.publishContent(publishContentDTO.getContentIds(), StpAdminUtil.getLoginUser());
		return R.ok();
	}

	/**
	 * 锁定内容
	 */
	@Log(title = "锁定内容", businessType = BusinessType.UPDATE)
	@PostMapping("/lock/{contentId}")
	public R<String> lock(@PathVariable("contentId") @LongId Long contentId) {
		this.contentService.lock(contentId, StpAdminUtil.getLoginUser().getUsername());
		return R.ok(StpAdminUtil.getLoginUser().getUsername());
	}

	/**
	 * 解锁内容
	 */
	@Log(title = "解锁内容", businessType = BusinessType.UPDATE)
	@PostMapping("/unlock/{contentId}")
	public R<String> unLock(@PathVariable("contentId") @LongId Long contentId) {
		this.contentService.unLock(contentId, StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	/**
	 * 复制内容
	 */
	@Log(title = "复制内容", businessType = BusinessType.UPDATE)
	@PostMapping("/copy")
	public R<?> copy(@RequestBody @Validated CopyContentDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.contentService.copy(dto);
		return R.ok();
	}

	/**
	 * 转移内容
	 */
	@Log(title = "转移内容", businessType = BusinessType.UPDATE)
	@PostMapping("/move")
	public R<?> move(@RequestBody @Validated MoveContentDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.contentService.move(dto);
		return R.ok();
	}

	/**
	 * 置顶
	 */
	@Log(title = "置顶", businessType = BusinessType.UPDATE)
	@PostMapping("/set_top")
	public R<?> setTop(@RequestBody @Validated SetTopContentDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.contentService.setTop(dto);
		return R.ok();
	}

	/**
	 * 取消置顶
	 */
	@Log(title = "取消置顶", businessType = BusinessType.UPDATE)
	@PostMapping("/cancel_top")
	public R<?> cancelTop(@RequestBody @NotEmpty List<Long> contentIds) {
		this.contentService.cancelTop(contentIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	/**
	 * 排序
	 */
	@Log(title = "内容排序", businessType = BusinessType.UPDATE)
	@PostMapping("/sort")
	public R<?> sort(@RequestBody @Validated SortContentDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.contentService.sort(dto);
		return R.ok();
	}

	/**
	 * 下线
	 */
	@Log(title = "下线内容", businessType = BusinessType.UPDATE)
	@PostMapping("/offline")
	public R<?> offline(@RequestBody @NotEmpty List<Long> contentIds) {
		this.contentService.offline(contentIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	/**
	 * 归档
	 */
	@Log(title = "归档内容", businessType = BusinessType.UPDATE)
	@PostMapping("/archive")
	public R<?> archive(@RequestBody @NotEmpty List<Long> contentIds) {
		this.contentService.archive(contentIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}
}
