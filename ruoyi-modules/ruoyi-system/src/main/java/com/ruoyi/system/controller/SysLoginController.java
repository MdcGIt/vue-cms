package com.ruoyi.system.controller;

import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.SystemConfig;
import com.ruoyi.system.domain.SysMenu;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.system.domain.dto.LoginBody;
import com.ruoyi.system.fixed.dict.LoginLogType;
import com.ruoyi.system.fixed.dict.SuccessOrFail;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.security.SysLoginService;
import com.ruoyi.system.service.ISysLogininforService;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysPermissionService;
import com.ruoyi.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 登录验证
 * 
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
public class SysLoginController extends BaseRestController {

	private final SysLoginService loginService;

	private final ISysMenuService menuService;

	private final ISysRoleService roleService;

	private final ISysPermissionService permissionService;

	private final ISysLogininforService logininfoService;

	private final AsyncTaskManager asyncTaskManager;

	/**
	 * 登录方法
	 * 
	 * @param loginBody 登录信息
	 * @return 结果
	 */
	@PostMapping("/login")
	public R<?> login(@Validated @RequestBody LoginBody loginBody) {
		// 生成令牌
		String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
				loginBody.getUuid());
		return R.ok(token);
	}

	@PostMapping("logout")
	public void logout() {
		try {
			if (StpAdminUtil.isLogin()) {
				LoginUser loginUser = StpAdminUtil.getLoginUser();
				StpAdminUtil.logout();
				asyncTaskManager.execute(this.logininfoService.recordLogininfor(loginUser.getUserType(),
						loginUser.getUserId(), loginUser.getUsername(), LoginLogType.LOGOUT, SuccessOrFail.SUCCESS,
						StringUtils.EMPTY));
			}
		} catch (Exception e) {

		}
	}

	/**
	 * 获取用户信息
	 * 
	 * @return 用户信息
	 */
	@SaAdminCheckLogin
	@GetMapping("getInfo")
	public R<?> getInfo() {
		LoginUser loginUser = StpAdminUtil.getLoginUser();
		SysUser user = (SysUser) loginUser.getUser();
		user.setAvatarSrc(SystemConfig.getResourcePrefix() + user.getAvatar());
		// 角色集合
		List<String> roles = this.roleService.selectRoleKeysByUserId(user.getUserId());
		// 权限集合
		List<String> permissions = loginUser.getPermissions();
		return R.ok(Map.of("user", user, "roles", roles, "permissions", permissions));
	}

	/**
	 * 获取路由信息
	 * 
	 * @return 路由信息
	 */
	@SaAdminCheckLogin
	@GetMapping("getRouters")
	public R<?> getRouters() {
		List<SysMenu> menus = this.menuService.lambdaQuery().orderByAsc(SysMenu::getOrderNum).list();

		List<String> permissions = StpAdminUtil.getLoginUser().getPermissions();
		if (!permissions.contains(ISysPermissionService.ALL_PERMISSION)) {
			menus = menus.stream().filter(m -> {
				return StringUtils.isEmpty(m.getPerms()) || permissions.contains(m.getPerms());
			}).toList();
		}
		// 国际化翻译
		I18nUtils.replaceI18nFields(menus, LocaleContextHolder.getLocale());
		// 上下级关系处理
		menus = menuService.getChildPerms(menus, 0);
		return R.ok(menuService.buildRouters(menus));
	}
}
