package com.ruoyi.system.controller;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.system.domain.SysMenu;
import com.ruoyi.system.domain.SysPermission;
import com.ruoyi.system.domain.dto.SysPermissionDTO;
import com.ruoyi.system.permission.MenuPermissionType;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysMenuService;
import com.ruoyi.system.service.ISysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 权限配置控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/permission")
public class SysPermissionController extends BaseRestController {

	private final ISysPermissionService permissionService;

	private final ISysMenuService menuService;
	
	private final MenuPermissionType menuPermissionType;

	@Priv(type = AdminUserType.TYPE)
	@Log(title = "权限设置", businessType = BusinessType.UPDATE)
	@PostMapping
	public R<?> saveMenuPermission(@Validated @RequestBody SysPermissionDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.permissionService.saveMenuPermissions(dto);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping("/menu")
	public R<?> getMenuPerms(@RequestParam String ownerType, @RequestParam String owner) {
		List<SysMenu> menus = this.menuService.lambdaQuery().orderByAsc(SysMenu::getOrderNum).list();
		SysPermission permission = this.permissionService.getPermissions(ownerType, owner);
		List<String> perms = List.of();
		if (Objects.nonNull(permission)) {
			String json  = permission.getPermissions().get(menuPermissionType.getId());
			perms = menuPermissionType.deserialize(json);
		}
		return R.ok(Map.of("menus", menus, "perms", perms));
	}
}
