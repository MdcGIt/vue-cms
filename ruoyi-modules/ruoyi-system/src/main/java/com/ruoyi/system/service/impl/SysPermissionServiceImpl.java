package com.ruoyi.system.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.security.SecurityUtils;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.SysConstants;
import com.ruoyi.system.domain.SysPermission;
import com.ruoyi.system.domain.SysRole;
import com.ruoyi.system.domain.dto.SysPermissionDTO;
import com.ruoyi.system.enums.PermissionOwnerType;
import com.ruoyi.system.exception.SysErrorCode;
import com.ruoyi.system.mapper.SysPermissionMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.permission.IPermissionType;
import com.ruoyi.system.service.ISysPermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
		implements ISysPermissionService {

	private final SysRoleMapper roleMapper;

	private final Map<String, IPermissionType> permissionTypes;

	private final RedisCache redisCache;

	IPermissionType getPermissionType(String type) {
		IPermissionType pt = this.permissionTypes.get(IPermissionType.BEAN_PREFIX + type);
		Assert.notNull(pt, () -> SysErrorCode.UNSUPPORTED_PERMISSION_TYPE.exception(type));
		return pt;
	}

	@Override
	public SysPermission getPermissions(String ownerType, String owner) {
		SysPermission permission = this.redisCache.getCacheObject(CACHE_KEY + ownerType + ":" + owner);
		if (Objects.isNull(permission)) {
			permission = this.lambdaQuery().eq(SysPermission::getOwnerType, ownerType)
					.eq(SysPermission::getOwner, owner).one();
			if (Objects.isNull(permission)) {
				permission = new SysPermission();
				permission.setOwnerType(ownerType);
				permission.setOwner(owner);
				permission.setPermissions(Map.of());
				permission.createBy(SysConstants.SYS_OPERATOR);
				this.save(permission);
			}
			this.redisCache.setCacheObject(CACHE_KEY + ownerType + ":" + owner, permission);
		}
		return permission;
	}

	@Override
	public void savePermissions(SysPermissionDTO dto) {
		Optional<SysPermission> opt = this.lambdaQuery().eq(SysPermission::getOwnerType, dto.getOwnerType())
				.eq(SysPermission::getOwner, dto.getOwner()).oneOpt();
		SysPermission db = opt.orElseGet(SysPermission::new);
		db.setOwnerType(dto.getOwnerType());
		db.setOwner(dto.getOwner());

		IPermissionType pt = this.getPermissionType(dto.getPermType());
		String permissionStr = pt.convert(dto.getPermissions().stream().toList());

		db.getPermissions().put(dto.getPermType(), permissionStr);
		db.updateBy(dto.getOperator().getUsername());
		if (StringUtils.isEmpty(db.getCreateBy())) {
			db.createBy(dto.getOperator().getUsername());
		}
		if (this.saveOrUpdate(db)) {
			this.redisCache.setCacheObject(CACHE_KEY + db.getOwnerType() + ":" + db.getOwner(), db);
		}
	}

	@Override
	public Set<String> getPermissionsByUser(Long userId, String permissionType) {
		IPermissionType pt = getPermissionType(permissionType);
		Set<String> permissions = new HashSet<>();
		if (SecurityUtils.isSuperAdmin(userId)) {
			permissions.add(ALL_PERMISSION);
		} else {
			// 用户权限
			SysPermission userPermission = this.getPermissions(PermissionOwnerType.User.name(), userId.toString());
			String json = userPermission.getPermissions().get(permissionType);
			if (StringUtils.isNotEmpty(json)) {
				permissions.addAll(pt.parse(json));
			}
			// 角色权限
			List<SysRole> roles = this.roleMapper.selectRolesByUserId(userId);
			roles.forEach(r -> {
				SysPermission permission = this.getPermissions(PermissionOwnerType.Role.name(),
						r.getRoleId().toString());
				String jsonRole = permission.getPermissions().get(permissionType);
				if (StringUtils.isNotEmpty(jsonRole)) {
					permissions.addAll(pt.parse(jsonRole));
				}
			});
		}
		return permissions;
	}

	@Override
	public Set<String> getPermissionListByUser(Long userId) {
		if (SecurityUtils.isSuperAdmin(userId)) {
			return Set.of(ALL_PERMISSION);
		}
		Set<String> permissions = new HashSet<>();
		// 用户权限
		SysPermission userPermission = this.getPermissions(PermissionOwnerType.User.name(), userId.toString());
		userPermission.getPermissions().entrySet().forEach(e -> {
			IPermissionType pt = getPermissionType(e.getKey());
			if (pt != null) {
				permissions.addAll(pt.parse(e.getValue()));
			}
		});
		// 角色权限
		List<SysRole> roles = this.roleMapper.selectRolesByUserId(userId);
		roles.forEach(r -> {
			SysPermission permission = this.getPermissions(PermissionOwnerType.Role.name(), r.getRoleId().toString());
			if (Objects.nonNull(permission)) {
				permission.getPermissions().entrySet().forEach(e -> {
					IPermissionType pt = getPermissionType(e.getKey());
					if (pt != null) {
						permissions.addAll(pt.parse(e.getValue()));
					}
				});
			}
		});
		return permissions;
	}
}
