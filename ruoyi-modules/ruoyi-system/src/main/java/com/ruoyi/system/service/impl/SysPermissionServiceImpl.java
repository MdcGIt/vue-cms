package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.Constants;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysPermission;
import com.ruoyi.system.domain.SysRole;
import com.ruoyi.system.domain.dto.SysPermissionDTO;
import com.ruoyi.system.enums.PermissionOwnerType;
import com.ruoyi.system.enums.PermissionType;
import com.ruoyi.system.mapper.SysPermissionMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.service.ISysPermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
		implements ISysPermissionService {

	private final SysRoleMapper roleMapper;
	
	private final RedisCache redisCache;
	
	@Override
	public SysPermission getPermissions(String ownerType, String owner) {
		SysPermission permission = this.redisCache.getCacheObject(CACHE_KEY + ownerType + ":" + owner);
		if (Objects.isNull(permission)) {
			permission = this.lambdaQuery()
					.eq(SysPermission::getOwnerType, ownerType).eq(SysPermission::getOwner, owner).one();
			if (Objects.nonNull(permission)) {
				this.redisCache.setCacheObject(CACHE_KEY + ownerType + ":" + owner, permission);
			}
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
		db.getPermissions().put(dto.getPermType(), dto.getPermissions().stream().toList());
		db.updateBy(dto.getOperator().getUsername());
		if (StringUtils.isEmpty(db.getCreateBy())) {
			db.createBy(dto.getOperator().getUsername());
		}
		if (this.saveOrUpdate(db)) {
			this.redisCache.setCacheObject(CACHE_KEY + db.getOwnerType() + ":" + db.getOwner(),  db);
		}
	}

	@Override
	public Map<String, List<String>> getPermissionMapByUser(Long userId) {
		Map<String, List<String>> permissions = new HashMap<>();
		if (Constants.SUPER_ADMIN == userId) {
			PermissionType[] values = PermissionType.values();
			for (PermissionType pt : values) {
				permissions.put(pt.name(), List.of(ALL_PERMISSION));
			}
		} else {
			List<SysRole> roles = this.roleMapper.selectRolesByUserId(userId);
			roles.forEach(r -> {
				SysPermission permission = this.getPermissions(PermissionOwnerType.Role.name(), r.getRoleId().toString());
				if (Objects.nonNull(permission)) {
					permission.getPermissions().entrySet().forEach(e -> {
						List<String> permKeys = permissions.get(e.getKey());
						if (Objects.isNull(permKeys)) {
							permissions.put(e.getKey(), e.getValue());
						} else {
							permKeys.addAll(e.getValue());
						}
					});
				}
			});
			PermissionType[] values = PermissionType.values();
			for (PermissionType pt : values) {
				if (!permissions.containsKey(pt.name())) {
					permissions.put(pt.name(), List.of());
				}
			}
		}
		return permissions;
	}

	@Override
	public List<String> getPermissionListByUser(Long userId) {
		if (Constants.SUPER_ADMIN == userId) {
			return List.of(ALL_PERMISSION);
		}
		ArrayList<String> permissions = new ArrayList<>();
		List<SysRole> roles = this.roleMapper.selectRolesByUserId(userId);
		roles.forEach(r -> {
			SysPermission permission = this.getPermissions(PermissionOwnerType.Role.name(), r.getRoleId().toString());
			if (Objects.nonNull(permission)) {
				permission.getPermissions().values().forEach(permissions::addAll);
			}
		});
		return permissions;
	}
}