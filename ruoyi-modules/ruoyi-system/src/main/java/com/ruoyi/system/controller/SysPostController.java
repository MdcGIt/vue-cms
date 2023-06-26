package com.ruoyi.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysPost;
import com.ruoyi.system.domain.vo.SysPostSelectVO;
import com.ruoyi.system.permission.SysMenuPriv;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysPostService;
import com.ruoyi.system.validator.LongId;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 岗位信息操作处理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/post")
public class SysPostController extends BaseRestController {

	private final ISysPostService postService;

	/**
	 * 获取岗位列表
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysPostList)
	@GetMapping("/list")
	public R<?> list(SysPost post) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysPost> q = new LambdaQueryWrapper<SysPost>()
				.like(StringUtils.isNotEmpty(post.getPostName()), SysPost::getPostName, post.getPostName())
				.like(StringUtils.isNotEmpty(post.getPostCode()), SysPost::getPostCode, post.getPostCode())
				.eq(StringUtils.isNotEmpty(post.getStatus()), SysPost::getStatus, post.getStatus())
				.orderByAsc(SysPost::getPostSort);
		Page<SysPost> page = postService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	@Log(title = "岗位管理", businessType = BusinessType.EXPORT)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysPostExport)
	@PostMapping("/export")
	public void export(HttpServletResponse response, SysPost post) {
		LambdaQueryWrapper<SysPost> q = new LambdaQueryWrapper<SysPost>()
				.like(StringUtils.isNotEmpty(post.getPostName()), SysPost::getPostName, post.getPostName())
				.like(StringUtils.isNotEmpty(post.getPostCode()), SysPost::getPostCode, post.getPostCode())
				.eq(StringUtils.isNotEmpty(post.getStatus()), SysPost::getStatus, post.getStatus())
				.orderByDesc(SysPost::getPostId);
		List<SysPost> list = postService.list(q);
		this.exportExcel(list, SysPost.class, response);
	}

	/**
	 * 根据岗位编号获取详细信息
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysPostList)
	@GetMapping(value = "/{postId}")
	public R<?> getInfo(@PathVariable @LongId Long postId) {
		return R.ok(postService.getById(postId));
	}

	/**
	 * 新增岗位
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysPostAdd)
	@Log(title = "岗位管理", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysPost post) {
		post.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		postService.insertPost(post);
		return R.ok();
	}

	/**
	 * 修改岗位
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysPostEdit)
	@Log(title = "岗位管理", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysPost post) {
		post.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		postService.updatePost(post);
		return R.ok();
	}

	/**
	 * 删除岗位
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysPostRemove)
	@Log(title = "岗位管理", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> postIds) {
		postService.deletePostByIds(postIds);
		return R.ok();
	}

	/**
	 * 获取岗位选择框列表
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/optionselect")
	public R<?> optionselect() {
		List<SysPostSelectVO> options = postService.list().stream()
				.map(post -> new SysPostSelectVO(post.getPostId(), post.getPostCode(), post.getPostName()))
				.collect(Collectors.toList());
		return R.ok(options);
	}
}
