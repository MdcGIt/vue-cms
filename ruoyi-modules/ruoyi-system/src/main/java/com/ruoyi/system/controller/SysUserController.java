package com.ruoyi.system.controller;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.SystemConfig;
import com.ruoyi.system.domain.SysDept;
import com.ruoyi.system.domain.SysPost;
import com.ruoyi.system.domain.SysRole;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.system.domain.dto.AuthRoleDTO;
import com.ruoyi.system.domain.dto.UserImportData;
import com.ruoyi.system.domain.vo.UserInfoVO;
import com.ruoyi.system.permission.SysMenuPriv;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysPostService;
import com.ruoyi.system.service.ISysRoleService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.impl.SysUserServiceImpl.SysUserReadListener;
import com.ruoyi.system.user.preference.IUserPreference;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * 用户信息
 * 
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user")
public class SysUserController extends BaseRestController {

	private final ISysUserService userService;

	private final ISysRoleService roleService;

	private final ISysDeptService deptService;

	private final ISysPostService postService;

	protected final Validator validator;

	/**
	 * 获取用户列表
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserList)
	@GetMapping("/list")
	public R<?> list(SysUser user) {
		PageRequest pr = this.getPageRequest();
		Page<SysUser> page = userService.lambdaQuery()
				.like(StringUtils.isNotEmpty(user.getUserName()), SysUser::getUserName, user.getUserName())
				.like(StringUtils.isNotEmpty(user.getPhonenumber()), SysUser::getPhonenumber, user.getPhonenumber())
				.eq(IdUtils.validate(user.getDeptId()), SysUser::getDeptId, user.getDeptId())
				.eq(Objects.nonNull(user.getStatus()), SysUser::getStatus, user.getStatus())
				.orderByDesc(SysUser::getUserId).page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
		page.getRecords().forEach(u -> {
			this.deptService.getDept(u.getDeptId()).ifPresent(d -> u.setDeptName(d.getDeptName()));
		});
		return bindDataTable(page);
	}

	@Log(title = "用户管理", businessType = BusinessType.EXPORT)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserExport)
	@PostMapping("/export")
	public void export(HttpServletResponse response, SysUser user) {
		LambdaQueryWrapper<SysUser> q = new LambdaQueryWrapper<SysUser>()
				.like(StringUtils.isNotEmpty(user.getUserName()), SysUser::getUserName, user.getUserName())
				.like(StringUtils.isNotEmpty(user.getPhonenumber()), SysUser::getPhonenumber, user.getPhonenumber())
				.eq(Objects.nonNull(user.getStatus()), SysUser::getStatus, user.getStatus())
				.orderByDesc(SysUser::getUserId);
		List<SysUser> list = userService.list(q);
		list.forEach(u -> {
			this.deptService.getDept(u.getDeptId()).ifPresent(d -> u.setDeptName(d.getDeptName()));
		});
		this.exportExcel(list, SysUser.class, response);
	}

	@Log(title = "用户管理", businessType = BusinessType.IMPORT)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserAdd)
	@PostMapping("/importData")
	public R<String> importData(MultipartFile file, boolean updateSupport) throws Exception {
		if (Objects.isNull(file) || file.isEmpty()) {
			return R.fail("Import file not exists!");
		}
		StringWriter logWriter = new StringWriter();
		SysUserReadListener readListener = new SysUserReadListener(this.userService, this.deptService, this.roleService,
				this.postService);
		readListener.setValidator(validator);
		readListener.setOperator(StpAdminUtil.getLoginUser().getUsername());
		readListener.setUpdateSupport(updateSupport);
		readListener.setLogWriter(logWriter);
		EasyExcel.read(file.getInputStream(), UserImportData.class, readListener)
				.locale(LocaleContextHolder.getLocale()).doReadAll();
		return R.ok(logWriter.toString());
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserAdd)
	@PostMapping("/importTemplate")
	public void importTemplate(HttpServletResponse response) {
		exportExcel(Collections.emptyList(), UserImportData.class, response);
	}

	/**
	 * 根据用户ID获取详细信息
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserList)
	@GetMapping(value = { "/", "/{userId}" })
	public R<?> getInfo(@PathVariable(value = "userId", required = false) Long userId) {
		List<SysPost> posts = postService.list();
		SysUser user = null;
		if (IdUtils.validate(userId)) {
			user = userService.getById(userId);
			user.setAvatarSrc(SystemConfig.getResourcePrefix() + user.getAvatar());
			user.setRoleIds(
					roleService.selectRolesByUserId(userId).stream().map(SysRole::getRoleId).toArray(Long[]::new));
			user.setPostIds(
					postService.selectPostListByUserId(userId).stream().map(SysPost::getPostId).toArray(Long[]::new));
		}
		return R.ok(UserInfoVO.builder().posts(posts).user(user).build());
	}

	/**
	 * 新增用户
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserAdd)
	@Log(title = "用户管理", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysUser user) {
		user.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		userService.insertUser(user);
		return R.ok();
	}

	/**
	 * 修改用户
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserEdit)
	@Log(title = "用户管理", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysUser user) {
		user.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		userService.updateUser(user);
		return R.ok();
	}

	/**
	 * 删除用户
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserRemove)
	@Log(title = "用户管理", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody List<Long> userIds) {
		boolean validate = IdUtils.validate(userIds);
		Assert.isTrue(validate, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception());

		userService.deleteUserByIds(userIds);
		return R.ok();
	}

	/**
	 * 重置密码
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserResetPwd)
	@Log(title = "用户管理", businessType = BusinessType.UPDATE, isSaveRequestData = false)
	@PutMapping("/resetPwd")
	public R<?> resetPwd(@RequestBody SysUser user) {
		userService.resetPwd(user);
		return R.ok();
	}

	/**
	 * 根据用户编号获取授权角色
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserList)
	@GetMapping("/authRole/{userId}")
	public R<?> authRole(@PathVariable("userId") Long userId) {
		SysUser user = userService.getById(userId);
		Assert.notNull(user, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(userId));

		List<SysRole> userRoles = roleService.selectRolesByUserId(userId);
		user.setRoleIds(userRoles.stream().map(SysRole::getRoleId).toArray(Long[]::new));
		List<SysRole> roles = this.roleService.list();
		return R.ok(UserInfoVO.builder().user(user).roles(roles).build());
	}

	/**
	 * 用户授权角色
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserEdit)
	@Log(title = "用户管理", businessType = BusinessType.GRANT)
	@PutMapping("/authRole")
	public R<?> insertAuthRole(@RequestBody AuthRoleDTO dto) {
		userService.insertUserAuth(dto.getUserId(), dto.getRoleIds());
		return R.ok();
	}

	/**
	 * 获取部门树列表
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysUserList)
	@GetMapping("/deptTree")
	public R<?> deptTree(SysDept dept) {
		List<SysDept> depts = this.deptService.list(new LambdaQueryWrapper<SysDept>()
				.like(StringUtils.isNotEmpty(dept.getDeptName()), SysDept::getDeptName, dept.getDeptName()));
		return R.ok(deptService.buildDeptTreeSelect(depts));
	}

	private final List<IUserPreference> userPreferenceList;

	@SaAdminCheckLogin
	@GetMapping("/getPreferences")
	public R<?> getPreferences() {
		SysUser user = this.userService.getById(StpAdminUtil.getLoginIdAsLong());
		return R.ok(Objects.isNull(user.getPreferences()) ? Map.of() : user.getPreferences());
	}

	@SaAdminCheckLogin
	@GetMapping("/preference")
	public R<?> getUserPreference(@RequestParam("id") @NotEmpty String id) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = (SysUser) loginUser.getUser();
		Optional<IUserPreference> findFirst = this.userPreferenceList.stream().filter(up -> up.getId().equals(id)).findFirst();
		if (!findFirst.isPresent()) {
			return R.fail();
		}
		Object object = user.getPreferences().getOrDefault(id, findFirst.get().getDefaultValue());
		return R.ok(object);
	}

	@SaAdminCheckLogin
	@PutMapping("/savePreferences")
	public R<?> saveUserPreferences(@RequestBody Map<String, Object> userPreferences) throws Exception {
		SysUser user = this.userService.getById(StpAdminUtil.getLoginIdAsLong());
		Map<String, Object> map = this.userPreferenceList.stream()
				.collect(Collectors.toMap(IUserPreference::getId, up -> userPreferences.getOrDefault(up.getId(), up.getDefaultValue())));
		user.setPreferences(map);
		this.userService.updateById(user);
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		loginUser.setUser(user);
		StpAdminUtil.setLoginUser(loginUser);
		return R.ok();
	}
}
