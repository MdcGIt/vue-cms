package com.ruoyi.system.controller;

import java.util.List;

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
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysDept;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysDeptService;

import lombok.RequiredArgsConstructor;

/**
 * 部门信息
 * 
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dept")
public class SysDeptController extends BaseRestController {

	private final ISysDeptService deptService;
	
	/**
	 * 获取部门列表
	 */
	@Priv(type = AdminUserType.TYPE, value = "system:dept:list")
	@GetMapping("/list")
	public R<?> list(SysDept dept) {
		LambdaQueryWrapper<SysDept> q = new LambdaQueryWrapper<SysDept>()
				.like(StringUtils.isNotEmpty(dept.getDeptName()), SysDept::getDeptName, dept.getDeptName())
				.eq(StringUtils.isNotEmpty(dept.getStatus()), SysDept::getStatus, dept.getStatus())
				.orderByAsc(SysDept::getParentId).orderByAsc(SysDept::getOrderNum);
		List<SysDept> list = deptService.list(q);
		return bindDataTable(list);
	}

	/**
	 * 根据部门编号获取详细信息
	 */
	@Priv(type = AdminUserType.TYPE, value = "system:dept:query")
	@GetMapping(value = "/{deptId}")
	public R<?> getInfo(@PathVariable Long deptId) {
		SysDept dept = deptService.getById(deptId);
		Assert.notNull(dept, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(deptId));
		this.deptService.getDept(dept.getParentId()).ifPresent(d -> dept.setParentName(d.getDeptName()));
		return R.ok(dept);
	}

	/**
	 * 新增部门
	 */
	@Priv(type = AdminUserType.TYPE, value = "system:dept:add")
	@Log(title = "部门管理", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysDept dept) {
		dept.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		deptService.insertDept(dept);
		return R.ok();
	}

	/**
	 * 修改部门
	 */
	@Priv(type = AdminUserType.TYPE, value = "system:dept:edit")
	@Log(title = "部门管理", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysDept dept) {
		dept.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		deptService.updateDept(dept);
		return R.ok();
	}

	/**
	 * 删除部门
	 */
	@Priv(type = AdminUserType.TYPE, value = "system:dept:remove")
	@Log(title = "部门管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{deptId}")
	public R<?> remove(@PathVariable Long deptId) {
		deptService.deleteDeptById(deptId);
		return R.ok();
	}
}
