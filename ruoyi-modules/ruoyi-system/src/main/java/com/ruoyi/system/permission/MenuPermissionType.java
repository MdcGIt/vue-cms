package com.ruoyi.system.permission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;

import cn.dev33.satoken.annotation.SaMode;

@Component(IPermissionType.BEAN_PREFIX + MenuPermissionType.ID)
public class MenuPermissionType implements IPermissionType<List<String>> {

	public static final String ID = "Menu";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "菜单权限";
	}

	@Override
	public List<String> parse(String json) {
		if (StringUtils.isEmpty(json)) {
			return List.of();
		}
		return JacksonUtils.fromList(json, String.class);
	}

	@Override
	public String convert(List<String> permissionKeys) {
		return JacksonUtils.to(permissionKeys);
	}

	@Override
	public String merge(List<String> permissionJsonList) {
		Set<String> set = new HashSet<>();
		permissionJsonList.forEach(json -> {
			set.addAll(parse(json));
		});
		return convert(set.stream().toList());
	}
	
	@Override
	public boolean hasPermission(List<String> permissionKeys, String json, SaMode mode) {
		List<String> perms = parse(json);
		if (mode == SaMode.AND) {
			for (String key : permissionKeys) {
				if(!perms.contains(key)) {
					return false;
				}
			}
			return true;
		} else {
			for (String key : permissionKeys) {
				if(perms.contains(key)) {
					return true;
				}
			}
			return false;
		}
	}
}
