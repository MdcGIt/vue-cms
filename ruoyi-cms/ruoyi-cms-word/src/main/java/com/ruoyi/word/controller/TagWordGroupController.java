package com.ruoyi.word.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.word.domain.CmsTagWordGroup;
import com.ruoyi.word.service.ITagWordGroupService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * TAG词分组前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/tagwordgroup")
public class TagWordGroupController extends BaseRestController {

	private final ISiteService siteService;

	private final ITagWordGroupService tagWordGroupService;

	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<CmsTagWordGroup> page = this.tagWordGroupService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), CmsTagWordGroup::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		page.getRecords().forEach(group -> {
			if (StringUtils.isNotEmpty(group.getLogo())) {
				group.setSrc(InternalUrlUtils.getActualPreviewUrl(group.getLogo()));
			}
		});
		return this.bindDataTable(page);
	}

	@GetMapping("/treedata")
	public R<?> getTreeData() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		List<CmsTagWordGroup> groups = this.tagWordGroupService.lambdaQuery()
				.eq(CmsTagWordGroup::getSiteId, site.getSiteId()).list();
		List<TreeNode<String>> treeData = this.tagWordGroupService.buildTreeData(groups);
		return R.ok(Map.of("rows", treeData, "siteName", site.getName()));
	}

	@PostMapping
	public R<?> add(@RequestBody CmsTagWordGroup group) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		group.setSiteId(site.getSiteId());
		group.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.tagWordGroupService.addTagWordGroup(group);
		return R.ok();
	}

	@PutMapping
	public R<?> edit(@RequestBody CmsTagWordGroup group) {
		group.updateBy(StpAdminUtil.getLoginUser().getUsername());
		this.tagWordGroupService.editTagWordGroup(group);
		return R.ok();
	}

	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> groupIds) {
		this.tagWordGroupService.deleteTagWordGroups(groupIds);
		return R.ok();
	}
}
