package com.ruoyi.system.permission;

import org.springframework.stereotype.Component;

@Component(IPermissionType.BEAN_PREFIX + MenuPermissionType.ID)
public class MenuPermissionType implements IPermissionType {

	public static final String ID = "Menu";
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "菜单权限";
	}
}
