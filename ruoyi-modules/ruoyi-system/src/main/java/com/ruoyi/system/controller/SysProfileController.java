package com.ruoyi.system.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.SecurityUtils;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IP2RegionUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.SystemConfig;
import com.ruoyi.system.domain.SysMenu;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.system.domain.vo.DashboardUserVO;
import com.ruoyi.system.domain.vo.ShortcutVO;
import com.ruoyi.system.domain.vo.UserProfileVO;
import com.ruoyi.system.enums.MenuType;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.*;
import com.ruoyi.system.user.preference.ShortcutUserPreference;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 个人信息 业务处理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseRestController {

	private final ISysUserService userService;

	private final ISysDeptService deptService;

	private final ISecurityConfigService securityConfigService;

	private final ISysMenuService menuService;

	private final ISysPermissionService permissionService;

	@Priv(type = AdminUserType.TYPE)
	@GetMapping
	public R<?> profile() {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = (SysUser) loginUser.getUser();
		user.setAvatarSrc(SystemConfig.getResourcePrefix() + user.getAvatar());
		String roleGroup = userService.selectUserRoleGroup(loginUser.getUserId());
		String postGroup = userService.selectUserPostGroup(loginUser.getUserId());
		return R.ok(new UserProfileVO(user, roleGroup, postGroup));
	}

	@Priv(type = AdminUserType.TYPE)
	@PutMapping
	@Log(title = "个人中心", businessType = BusinessType.UPDATE)
	public R<?> updateProfile(@RequestBody SysUser user) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();

		boolean checkPhoneUnique = this.userService.checkPhoneUnique(user.getPhonenumber(), loginUser.getUserId());
		Assert.isTrue(checkPhoneUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("PhoneNumber"));

		boolean checkEmailUnique = this.userService.checkPhoneUnique(user.getEmail(), loginUser.getUserId());
		Assert.isTrue(checkEmailUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("Email"));

		SysUser sysUser = (SysUser) loginUser.getUser();
		sysUser.setNickName(user.getNickName());
		sysUser.setPhonenumber(user.getPhonenumber());
		sysUser.setEmail(user.getEmail());
		sysUser.setSex(user.getSex());

		LambdaUpdateWrapper<SysUser> q = new LambdaUpdateWrapper<SysUser>()
				.set(SysUser::getNickName, user.getNickName()).set(SysUser::getPhonenumber, user.getPhonenumber())
				.set(SysUser::getEmail, user.getEmail()).set(SysUser::getSex, user.getSex())
				.eq(SysUser::getUserId, loginUser.getUserId());
		if (this.userService.update(q)) {
			StpAdminUtil.setLoginUser(loginUser);
			return R.ok();
		}
		return R.fail();
	}

	@Priv(type = AdminUserType.TYPE)
	@Log(title = "个人中心", businessType = BusinessType.UPDATE, isSaveRequestData = false)
	@PutMapping("/updatePwd")
	public R<?> updatePwd(String oldPassword, String newPassword) {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = userService.getById(loginUser.getUserId());
		if (!SecurityUtils.matches(oldPassword, user.getPassword())) {
			return R.fail("修改密码失败，旧密码错误");
		}
		if (SecurityUtils.matches(newPassword, user.getPassword())) {
			return R.fail("新密码不能与旧密码相同");
		}
		// 密码安全规则校验
		this.securityConfigService.validPassword(user, newPassword);

		boolean update = this.userService.lambdaUpdate()
				.set(SysUser::getPassword, SecurityUtils.passwordEncode(newPassword))
				.eq(SysUser::getUserId, loginUser.getUserId()).update();
		return update ? R.ok() : R.fail();
	}

	@Priv(type = AdminUserType.TYPE)
	@Log(title = "个人中心", businessType = BusinessType.UPDATE)
	@PostMapping("/avatar")
	public R<?> avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception {
		if (Objects.isNull(file) || file.isEmpty()) {
			return R.fail("上传图片异常，请联系管理员");
		}
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		String avatar = this.userService.uploadAvatar(loginUser.getUserId(), file);
		// 更新缓存用户头像
		SysUser user = (SysUser) loginUser.getUser();
		user.setAvatar(avatar);
		StpAdminUtil.setLoginUser(loginUser);
		return R.ok(SystemConfig.getResourcePrefix() + avatar);
	}

	/**
	 * 首页用户信息
	 */
	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/homeInfo")
	public R<?> getHomeInfo() {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = (SysUser) loginUser.getUser();
		StpAdminUtil.setLoginUser(loginUser);
		DashboardUserVO vo = new DashboardUserVO();
		vo.setUserName(user.getUserName());
		vo.setNickName(user.getNickName());
		vo.setLastLoginTime(user.getLoginDate());
		vo.setLastLoginIp(user.getLoginIp());
		vo.setLastLoginAddr(IP2RegionUtils.ip2Region(user.getLoginIp()));
		if (StringUtils.isNotEmpty(user.getAvatar())) {
			vo.setAvatar(SystemConfig.getResourcePrefix() + user.getAvatar());
		}
		this.deptService.getDept(user.getDeptId()).ifPresent(dept -> vo.setDeptName(dept.getDeptName()));
		return R.ok(vo);
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/shortcuts")
	public R<?> getHomeShortcuts() {
		SysUser user = this.userService.getById(StpAdminUtil.getLoginIdAsLong());
		List<Long> menuIds = ShortcutUserPreference.getValue(user.getPreferences());
		List<SysMenu> menus = this.menuService.lambdaQuery().eq(SysMenu::getMenuType, MenuType.Menu.value())
				.in(menuIds.size() > 0, SysMenu::getMenuId, menuIds).last("limit 8").list();

		List<String> menuPerms = StpAdminUtil.getLoginUser().getPermissions();
		if (!menuPerms.contains(ISysPermissionService.ALL_PERMISSION)) {
			menus = menus.stream().filter(m -> {
				return StringUtils.isEmpty(m.getPerms()) || menuPerms.contains(m.getPerms());
			}).toList();
		}
		I18nUtils.replaceI18nFields(menus, LocaleContextHolder.getLocale());
		List<ShortcutVO> result = menus.stream()
				.sorted(Comparator.comparingInt(m -> menuIds.indexOf(m.getMenuId())))
				.map(m -> new ShortcutVO(m.getMenuName(), m.getIcon(), StringUtils.capitalize(m.getPath()))).toList();
		return R.ok(result);
	}
}
