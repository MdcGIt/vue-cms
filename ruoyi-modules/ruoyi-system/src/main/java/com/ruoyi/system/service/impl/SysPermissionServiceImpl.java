package com.ruoyi.system.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.security.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.SysConstants;
import com.ruoyi.system.domain.SysPermission;
import com.ruoyi.system.domain.SysRole;
import com.ruoyi.system.domain.dto.SysPermissionDTO;
import com.ruoyi.system.enums.PermissionOwnerType;
import com.ruoyi.system.mapper.SysPermissionMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.permission.MenuPermissionType;
import com.ruoyi.system.service.ISysPermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
		implements ISysPermissionService {

	private final SysRoleMapper roleMapper;

	private final MenuPermissionType menuPermissionType;

	@Override
	public SysPermission getPermissions(String ownerType, String owner) {
		SysPermission permission = this.lambdaQuery().eq(SysPermission::getOwnerType, ownerType).eq(SysPermission::getOwner, owner).one();
		if (Objects.isNull(permission)) {
			permission = new SysPermission();
			permission.setOwnerType(ownerType);
			permission.setOwner(owner);
			permission.setPermissions(Map.of());
			permission.createBy(SysConstants.SYS_OPERATOR);
			this.save(permission);
		}
		return permission;
	}

	@Override
	public void saveMenuPermissions(SysPermissionDTO dto) {
		Optional<SysPermission> opt = this.lambdaQuery().eq(SysPermission::getOwnerType, dto.getOwnerType())
				.eq(SysPermission::getOwner, dto.getOwner()).oneOpt();
		SysPermission db = opt.orElseGet(SysPermission::new);
		db.setOwnerType(dto.getOwnerType());
		db.setOwner(dto.getOwner());

		String permissionStr = menuPermissionType.convert(dto.getPermissions().stream().toList());
		db.getPermissions().put(dto.getPermType(), permissionStr);
		db.updateBy(dto.getOperator().getUsername());
		if (StringUtils.isEmpty(db.getCreateBy())) {
			db.createBy(dto.getOperator().getUsername());
		}
		this.saveOrUpdate(db);
	}
	
	@Override
	public Set<String> getMenuPermissionsByUser(Long userId) {
		Set<String> permissions = new HashSet<>();
		if (SecurityUtils.isSuperAdmin(userId)) {
			permissions.add(ALL_PERMISSION);
		} else {
			// 用户权限
			SysPermission userPermission = this.getPermissions(PermissionOwnerType.User.name(), userId.toString());
			String json = userPermission.getPermissions().get(menuPermissionType.getId());
			if (StringUtils.isNotEmpty(json)) {
				permissions.addAll(menuPermissionType.parse(json));
			}
			// 角色权限
			List<SysRole> roles = this.roleMapper.selectRolesByUserId(userId);
			roles.forEach(r -> {
				SysPermission permission = this.getPermissions(PermissionOwnerType.Role.name(),
						r.getRoleId().toString());
				String jsonRole = permission.getPermissions().get(menuPermissionType.getId());
				if (StringUtils.isNotEmpty(jsonRole)) {
					permissions.addAll(menuPermissionType.parse(jsonRole));
				}
			});
		}
		return permissions;
	}
}
