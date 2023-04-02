package com.ruoyi.contentcore.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.RecoverRecycleContentDTO;
import com.ruoyi.contentcore.domain.vo.RecycleContentVO;
import com.ruoyi.contentcore.mapper.CmsContentMapper;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.CatalogUtils;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.ruoyi.system.security.SaAdminCheckLogin;

import lombok.RequiredArgsConstructor;

@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/content/recycle")
public class RecycleContentController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final CmsContentMapper contentMapper;

	/**
	 * 内容列表
	 */
	@GetMapping
	public R<?> getRecycleContentList(@RequestParam(name = "catalogId", required = false) Long catalogId,
			@RequestParam(name = "cotnentType", required = false) String cotnentType,
			@RequestParam(name = "title", required = false) String title,
			@RequestParam(name = "status", required = false) String status) {
		PageRequest pr = getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		Page<RecycleContentVO> page = this.contentMapper.selectRecycleContentList(
				new Page<>(pr.getPageNumber(), pr.getPageSize(), true), site.getSiteId(), catalogId, cotnentType,
				status, title);
		return this.bindDataTable(page);
	}

	@PostMapping("recover")
	public R<?> recoverContent(@RequestBody @Validated RecoverRecycleContentDTO dto) {
		List<CmsContent> contents = contentMapper.selectRecycleContentListByBackupIds(dto.getBackupIds());
		for (CmsContent content : contents) {
			Long catalogId = IdUtils.validate(dto.getCatalogId()) ? dto.getCatalogId() : content.getCatalogId();
			CmsCatalog catalog = this.catalogService.getCatalog(catalogId);
			Assert.notNull(catalog, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("catalogId", catalogId));
			content.setCatalogId(catalog.getCatalogId());
			content.setCatalogAncestors(catalog.getAncestors());
			content.setTopCatalog(CatalogUtils.getTopCatalog(catalog));
			this.contentMapper.insert(content);
			this.contentMapper.deleteBackupByContentId(content.getContentId());

			IContentType contentType = ContentCoreUtils.getContentType(content.getContentType());
			contentType.recover(content);
		}
		return R.ok();
	}
}
