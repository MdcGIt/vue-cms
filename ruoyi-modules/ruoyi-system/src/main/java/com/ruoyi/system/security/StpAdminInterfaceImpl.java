package com.ruoyi.system.security;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.ConvertUtils;
import com.ruoyi.system.service.ISysPermissionService;
import com.ruoyi.system.service.ISysRoleService;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;

/**
 * SaToken 权限缓存数据接口
 */
@Component
@RequiredArgsConstructor
public class StpAdminInterfaceImpl implements StpInterface {
	
	private final ISysPermissionService permissionService;
	
	private final ISysRoleService roleService;
	
	@Override
	public List<String> getPermissionList(Object loginId, String loginType) {
		return this.permissionService.getPermissionListByUser(ConvertUtils.toLong(loginId));
	}
	

	@Override
	public List<String> getRoleList(Object loginId, String loginType) {
		return roleService.selectRoleKeysByUserId(ConvertUtils.toLong(loginId));
	}
}
