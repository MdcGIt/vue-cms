package com.ruoyi.word.controller;

import java.util.ArrayList;
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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.SortUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.word.domain.CmsHotWordGroup;
import com.ruoyi.word.service.IHotWordGroupService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 热词分组前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/hotwordgroup")
public class HotWordGroupController extends BaseRestController {

	private final ISiteService siteService;

	private final IHotWordGroupService hotWordGroupService;

	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<CmsHotWordGroup> page = this.hotWordGroupService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), CmsHotWordGroup::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@GetMapping("/options")
	public R<?> getOptions() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		List<CmsHotWordGroup> list = this.hotWordGroupService.getHotWordGroupsBySiteId(site.getSiteId());
		List<Map<String, Object>> options = new ArrayList<>();
		list.forEach(g -> {
			options.add(Map.of("groupId", g.getGroupId(), "name", g.getName()));
		});
		return this.bindDataTable(options);
	}

	@GetMapping("/treedata")
	public R<?> getTreeData() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		LambdaQueryWrapper<CmsHotWordGroup> q = new LambdaQueryWrapper<CmsHotWordGroup>().eq(CmsHotWordGroup::getSiteId,
				site.getSiteId());
		List<CmsHotWordGroup> groups = this.hotWordGroupService.list(q);
		List<TreeNode<String>> list = new ArrayList<>();
		if (groups != null && groups.size() > 0) {
			groups.forEach(c -> {
				TreeNode<String> treeNode = new TreeNode<>(String.valueOf(c.getGroupId()), "", c.getName(), true);
				treeNode.setProps(Map.of("code", c.getCode()));
				list.add(treeNode);
			});
		}
		List<TreeNode<String>> treeData = TreeNode.build(list);
		return R.ok(Map.of("rows", treeData, "siteName", site.getName()));
	}

	@PostMapping
	public R<?> add(@RequestBody CmsHotWordGroup group) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		if (!checkUnique(site.getSiteId(), null, group.getName(), group.getCode())) {
			return R.fail("分组名称或编码冲突");
		}
		group.setSiteId(site.getSiteId());
		group.setGroupId(IdUtils.getSnowflakeId());
		group.setSortFlag(SortUtils.getDefaultSortValue());
		group.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.hotWordGroupService.save(group);
		return R.ok(group.getGroupId());
	}

	@PutMapping
	public R<String> edit(@RequestBody CmsHotWordGroup group) {
		CmsHotWordGroup dbGroup = this.hotWordGroupService.getById(group.getGroupId());
		Assert.notNull(dbGroup, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("groupId", group.getGroupId()));
		
		if (!checkUnique(dbGroup.getSiteId(), group.getGroupId(), group.getName(), group.getCode())) {
			return R.fail("分组名称或编码冲突");
		}
		dbGroup.setName(group.getName());
		dbGroup.setCode(group.getCode());
		dbGroup.setRemark(group.getRemark());
		dbGroup.updateBy(StpAdminUtil.getLoginUser().getUsername());
		this.hotWordGroupService.updateById(group);
		return R.ok();
	}

	private boolean checkUnique(Long siteId, Long groupId, String name, String code) {
		LambdaQueryWrapper<CmsHotWordGroup> q = new LambdaQueryWrapper<CmsHotWordGroup>()
				.eq(CmsHotWordGroup::getSiteId, siteId)
				.ne(groupId != null && groupId > 0, CmsHotWordGroup::getGroupId, groupId)
				.and(wrapper -> wrapper.eq(CmsHotWordGroup::getName, name).or().eq(CmsHotWordGroup::getCode, code));
		return this.hotWordGroupService.count(q) == 0;
	}

	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> groupIds) {
		this.hotWordGroupService.deleteHotWordGroups(groupIds);
		return R.ok();
	}
}
