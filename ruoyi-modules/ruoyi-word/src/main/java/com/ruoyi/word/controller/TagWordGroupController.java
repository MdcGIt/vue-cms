package com.ruoyi.word.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
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
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.word.domain.TagWordGroup;
import com.ruoyi.word.permission.WordPriv;
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
@Priv(type = AdminUserType.TYPE, value = WordPriv.View)
@RequiredArgsConstructor
@RestController
@RequestMapping("/word/tagword/group")
public class TagWordGroupController extends BaseRestController {

	private final ITagWordGroupService tagWordGroupService;

	@GetMapping
	public R<?> getPageList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<TagWordGroup> page = this.tagWordGroupService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), TagWordGroup::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@GetMapping("/treedata")
	public R<?> getTreeData() {
		List<TagWordGroup> groups = this.tagWordGroupService.list();
		List<TreeNode<String>> treeData = this.tagWordGroupService.buildTreeData(groups);
		return R.ok(treeData);
	}

	@PostMapping
	public R<?> add(@RequestBody @Validated TagWordGroup group) {
		group.createBy(StpAdminUtil.getLoginUser().getUsername());
		return R.ok(this.tagWordGroupService.addTagWordGroup(group));
	}

	@PutMapping
	public R<?> edit(@RequestBody @Validated TagWordGroup group) {
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
