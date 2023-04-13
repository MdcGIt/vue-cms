package com.ruoyi.contentcore.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.vo.RecycleContentVO;
import com.ruoyi.contentcore.mapper.CmsContentMapper;
import com.ruoyi.contentcore.perms.ContentCorePriv;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@Priv(type = AdminUserType.TYPE, value = ContentCorePriv.ContentView)
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/content/recycle")
public class RecycleContentController extends BaseRestController {

	private final ISiteService siteService;

	private final IContentService contentService;
	
	private final CmsContentMapper contentMapper;

	@GetMapping
	public R<?> getRecycleContentList(@RequestParam(name = "catalogId", required = false) Long catalogId,
			@RequestParam(name = "contentType", required = false) String contentType,
			@RequestParam(name = "title", required = false) String title,
			@RequestParam(name = "status", required = false) String status) {
		PageRequest pr = getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		if (StringUtils.isNotEmpty(title)) {
			title = "%" + title + "%";
		}
		Page<RecycleContentVO> page = this.contentMapper.selectRecycleContentList(
				new Page<>(pr.getPageNumber(), pr.getPageSize(), true), site.getSiteId(), catalogId, contentType,
				status, title);
		return this.bindDataTable(page);
	}

	@Log(title = "恢复回收站内容", businessType = BusinessType.INSERT)
	@PostMapping("/recover")
	public R<?> recoverContent(@RequestBody @NotEmpty List<Long> backupIds) {
		this.contentService.recoverContents(backupIds, StpAdminUtil.getLoginUser());
		return R.ok();
	}

	@Log(title = "删除回收站内容", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> deleteRecycleContents(@RequestBody @NotEmpty List<Long> backupIds) {
		this.contentService.deleteRecycleContents(backupIds);
		return R.ok();
	}
}
