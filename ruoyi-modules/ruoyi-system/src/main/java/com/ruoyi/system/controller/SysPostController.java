package com.ruoyi.system.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysPost;
import com.ruoyi.system.domain.vo.SysPostSelectVO;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysPostService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

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
	@Priv(type = AdminUserType.TYPE, value = "system:post:list")
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
	@Priv(type = AdminUserType.TYPE, value = "system:post:export")
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
	@Priv(type = AdminUserType.TYPE, value = "system:post:query")
	@GetMapping(value = "/{postId}")
	public R<?> getInfo(@PathVariable Long postId) {
		return R.ok(postService.getById(postId));
	}

	/**
	 * 新增岗位
	 */
	@Priv(type = AdminUserType.TYPE, value = "system:post:add")
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
	@Priv(type = AdminUserType.TYPE, value = "system:post:edit")
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
	@Priv(type = AdminUserType.TYPE, value = "system:post:remove")
	@Log(title = "岗位管理", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody List<Long> postIds) {
		Assert.isTrue(IdUtils.validate(postIds), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception());
		postService.deletePostByIds(postIds);
		return R.ok();
	}

	/**
	 * 获取岗位选择框列表
	 */
	@SaAdminCheckLogin
	@GetMapping("/optionselect")
	public R<?> optionselect() {
		List<SysPostSelectVO> options = postService.list().stream()
				.map(post -> new SysPostSelectVO(post.getPostId(), post.getPostCode(), post.getPostName()))
				.collect(Collectors.toList());
		return R.ok(options);
	}
}