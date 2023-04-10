package com.ruoyi.system.service;

import java.util.Set;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.SysPermission;
import com.ruoyi.system.domain.dto.SysPermissionDTO;

/**
 * 通用权限 业务层
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface ISysPermissionService extends IService<SysPermission> {

	public String CACHE_KEY = "sys:perms:";
	
	/** 所有权限标识 */
	public static final String ALL_PERMISSION = "*";

	public static final String DELIMETER = ",";

	/**
	 * 获取权限信息
	 * 
	 * @param ownerType
	 * @param owner
	 * @return
	 */
	public SysPermission getPermissions(String ownerType, String owner);

	/**
	 * 获取用户菜单权限集合
	 * 
	 * @param userId
	 * @return
	 */
	public Set<String> getMenuPermissionsByUser(Long userId);
	
	/**
	 * 保存菜单权限信息
	 * 
	 * @param dto
	 */
	public void saveMenuPermissions(SysPermissionDTO dto);
}
