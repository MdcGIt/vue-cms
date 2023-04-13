package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

	@Override
	public SysPermission getPermissions(String ownerType, String owner) {
		return this.lambdaQuery().eq(SysPermission::getOwnerType, ownerType).eq(SysPermission::getOwner, owner).one();
	}

	@Override
	public void saveMenuPermissions(SysPermissionDTO dto) {
		Optional<SysPermission> opt = this.lambdaQuery().eq(SysPermission::getOwnerType, dto.getOwnerType())
				.eq(SysPermission::getOwner, dto.getOwner()).oneOpt();
		SysPermission db = opt.orElseGet(SysPermission::new);
		db.setOwnerType(dto.getOwnerType());
		db.setOwner(dto.getOwner());

		String permissionStr = menuPermissionType.serialize(dto.getPermissions().stream().toList());
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
				permissions.addAll(menuPermissionType.deserialize(json));
			}
			// 角色权限
			List<SysRole> roles = this.roleMapper.selectRolesByUserId(userId);
			roles.forEach(r -> {
				SysPermission permission = this.getPermissions(PermissionOwnerType.Role.name(),
						r.getRoleId().toString());
				if (permission != null) {
					String jsonRole = permission.getPermissions().get(menuPermissionType.getId());
					if (StringUtils.isNotEmpty(jsonRole)) {
						permissions.addAll(menuPermissionType.deserialize(jsonRole));
					}
				}
			});
		}
		return permissions;
	}

	private final Map<String, IPermissionType<?>> permissionTypes;

	private IPermissionType<?> getPermissionType(String type) {
		return this.permissionTypes.get(IPermissionType.BEAN_PREFIX + type);
	}

	@Override
	public Map<String, String> getUserPermissions(Long userId) {
		Map<String, List<String>> typePermStrs = permissionTypes.values().stream()
				.collect(Collectors.toMap(IPermissionType::getId, pt -> new ArrayList<>()));
		// 用户权限
		SysPermission userPermission = this.getPermissions(PermissionOwnerType.User.name(), userId.toString());
		if (userPermission != null) {
			typePermStrs.entrySet().forEach(e -> {
				String json = userPermission.getPermissions().get(e.getKey());
				if (StringUtils.isNotEmpty(json)) {
					e.getValue().add(json);
				}
			});
		}
		// 角色权限
		List<Long> roleIds = this.roleMapper.selectRolesByUserId(userId).stream().map(role -> role.getRoleId())
				.toList();
		if (!roleIds.isEmpty()) {
			List<SysPermission> rolePermissions = this.lambdaQuery()
					.eq(SysPermission::getOwnerType, PermissionOwnerType.Role.name())
					.in(SysPermission::getOwner, roleIds).list();
			rolePermissions.forEach(rp -> {
				typePermStrs.entrySet().forEach(e -> {
					String json = rp.getPermissions().get(e.getKey());
					if (StringUtils.isNotEmpty(json)) {
						e.getValue().add(json);
					}
				});
			});
		}
		Map<String, String> result = new HashMap<>();
		typePermStrs.entrySet().forEach(e -> {
			IPermissionType<?> pt = this.getPermissionType(e.getKey());
			String mergedJson = pt.merge(e.getValue());
			result.put(e.getKey(), mergedJson);
		});
		return result;
	}

	@Override
	public void resetLoginUserPermissions(LoginUser loginUser) {
		Map<String, String> userPermissions = getUserPermissions(loginUser.getUserId());
		loginUser.setPermissions(userPermissions);
		StpAdminUtil.getTokenValueListByLoginId(loginUser.getUserId()).forEach(token -> {
			SaSession session = StpAdminUtil.getTokenSessionByToken(token);
			LoginUser lu = (LoginUser) session.get(SaSession.USER);
			lu.setPermissions(userPermissions);
			session.set(SaSession.USER, loginUser);
		});
	}
}
