package com.ruoyi.system.permission;

import java.util.List;

import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;

public interface IPermissionType {

	public static final String BEAN_PREFIX = "PermissionType_";
	
	public String getId();
	
	public String getName();
	
	/**
	 * 将存储在sys_permission中的权限字符串解析成权限项列表
	 * 
	 * @param json
	 * @return
	 */
	default public List<String> parsePermissionKeys(String json) {
		if (StringUtils.isEmpty(json)) {
			return List.of();
		}
		return JacksonUtils.fromList(json, String.class);
	}

	/**
	 * 转权限项列表转成持久化存储字符串
	 * 
	 * @param permissionKeys
	 * @return
	 */
	default public String convert(List<String> permissionKeys) {
		return JacksonUtils.to(permissionKeys);
	}
}
