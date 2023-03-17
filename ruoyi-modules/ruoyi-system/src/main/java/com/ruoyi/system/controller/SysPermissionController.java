package com.ruoyi.system.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.system.domain.SysMenu;
import com.ruoyi.system.domain.SysPermission;
import com.ruoyi.system.domain.dto.SysPermissionDTO;
import com.ruoyi.system.enums.PermissionType;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysPermissionService;

import lombok.RequiredArgsConstructor;

/**
 * 权限配置控制器
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/permission")
public class SysPermissionController extends BaseRestController {

	private final ISysPermissionService permissionService;

	private final ISysMenuService menuService;

	@SaAdminCheckLogin
	@Log(title = "权限设置", businessType = BusinessType.UPDATE)
	@PostMapping
	public R<?> savePermission(@RequestBody SysPermissionDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.permissionService.savePermissions(dto);
		return R.ok();
	}

	@SaAdminCheckLogin
	@GetMapping("/menu")
	public R<?> getMenuPerms(@RequestParam String ownerType, @RequestParam String owner) {
		List<SysMenu> menus = this.menuService.lambdaQuery().orderByAsc(SysMenu::getOrderNum).list();
		SysPermission permission = this.permissionService.getPermissions(ownerType, owner);
		List<String> perms = Collections.emptyList();
		if (Objects.nonNull(permission)) {
			perms = permission.getPermissions().getOrDefault(PermissionType.Menu.name(), Collections.emptyList());
		}
		return R.ok(Map.of("menus", menus, "perms", perms));
	}
}
