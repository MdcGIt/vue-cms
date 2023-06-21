package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.ruoyi.common.utils.IdUtils;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.security.SecurityUtils;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysPermission;
import com.ruoyi.system.domain.SysRole;
import com.ruoyi.system.domain.dto.SysPermissionDTO;
import com.ruoyi.system.enums.PermissionOwnerType;
import com.ruoyi.system.mapper.SysPermissionMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.permission.IPermissionType;
import com.ruoyi.system.permission.MenuPermissionType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysPermissionService;

import cn.dev33.satoken.session.SaSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission>
		implements ISysPermissionService {

	private final SysRoleMapper roleMapper;

	private final MenuPermissionType menuPermissionType;

	private final Map<String, IPermissionType<?>> permissionTypes;

	private IPermissionType<?> getPermissionType(String type) {
		return this.permissionTypes.get(IPermissionType.BEAN_PREFIX + type);
	}

	@Override
	public SysPermission getPermissions(String ownerType, String owner) {
		return this.lambdaQuery().eq(SysPermission::getOwnerType, ownerType).eq(SysPermission::getOwner, owner).one();
	}

	@Override
	public void saveMenuPermissions(SysPermissionDTO dto) {
		Optional<SysPermission> opt = this.lambdaQuery().eq(SysPermission::getOwnerType, dto.getOwnerType())
				.eq(SysPermission::getOwner, dto.getOwner()).oneOpt();

		SysPermission db = opt.orElseGet(() -> {
			SysPermission p = new SysPermission();
			p.setPermId(IdUtils.getSnowflakeId());
			p.setOwnerType(dto.getOwnerType());
			p.setOwner(dto.getOwner());
			p.createBy(dto.getOperator().getUsername());
			return p;
		});

		String permissionStr = menuPermissionType.serialize(dto.getPermissions().stream().toList());
		db.getPermissions().put(dto.getPermType(), permissionStr);
		db.updateBy(dto.getOperator().getUsername());
		if (StringUtils.isEmpty(db.getCreateBy())) {
			db.createBy(dto.getOperator().getUsername());
		}
		this.saveOrUpdate(db);
	}

	@Override
	public Set<String> getUserPermissions(Long userId, @Nullable String permissionType) {
		Set<String> permissions = new HashSet<>();
		if (SecurityUtils.isSuperAdmin(userId)) {
			permissions.add(ALL_PERMISSION);
		} else {
			// 用户权限
			SysPermission userPermission = this.getPermissions(PermissionOwnerType.User.name(), userId.toString());
			if (userPermission != null) {
				this.permissionTypes.values().forEach(pt -> {
					if (StringUtils.isEmpty(permissionType) || pt.getId().equals(permissionType)) {
						String json = userPermission.getPermissions().get(pt.getId());
						if (StringUtils.isNotEmpty(json)) {
							permissions.addAll(pt.convert(json));
						}
					}
				});
			}
			// 角色权限
			List<SysRole> roles = this.roleMapper.selectRolesByUserId(userId);
			roles.forEach(r -> {
				SysPermission permission = this.getPermissions(PermissionOwnerType.Role.name(),
						r.getRoleId().toString());
				if (permission != null) {
					this.permissionTypes.values().forEach(pt -> {
						if (StringUtils.isEmpty(permissionType) || pt.getId().equals(permissionType)) {
							String json = permission.getPermissions().get(pt.getId());
							if (StringUtils.isNotEmpty(json)) {
								permissions.addAll(pt.convert(json));
							}
						}
					});
				}
			});
		}
		return permissions;
	}

	@Override
	public void resetLoginUserPermissions(LoginUser loginUser) {
		List<String> userPermissions = getUserPermissions(loginUser.getUserId(), null).stream().toList();
		loginUser.setPermissions(userPermissions);
		StpAdminUtil.getTokenValueListByLoginId(loginUser.getUserId()).forEach(token -> {
			SaSession session = StpAdminUtil.getTokenSessionByToken(token);
			LoginUser lu = (LoginUser) session.get(SaSession.USER);
			lu.setPermissions(userPermissions);
			session.set(SaSession.USER, loginUser);
		});
	}
}
