package com.ruoyi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.system.domain.SysPermission;
import com.ruoyi.system.domain.dto.SysPermissionDTO;
import jakarta.annotation.Nullable;

import java.util.Set;

/**
 * 通用权限 业务层
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface ISysPermissionService extends IService<SysPermission> {

	/** 所有权限标识 */
	String ALL_PERMISSION = "*";

	/**
	 * 获取权限信息
	 * 
	 * @param ownerType
	 * @param owner
	 * @return
	 */
	SysPermission getPermissions(String ownerType, String owner);

	/**
	 * 保存菜单权限信息
	 * 
	 * @param dto
	 */
	void saveMenuPermissions(SysPermissionDTO dto);

	/**
	 * 获取用户权限列表
	 * 
	 * @param userId
	 * @param permissionType 指定权限类型
	 * @return
	 */
	Set<String> getUserPermissions(Long userId, @Nullable String permissionType);

	/**
	 * 重置登录用户权限信息
	 */
	void resetLoginUserPermissions(LoginUser loginUser);
}
