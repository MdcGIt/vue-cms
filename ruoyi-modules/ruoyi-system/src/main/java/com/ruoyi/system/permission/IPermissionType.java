package com.ruoyi.system.permission;

import java.util.List;

import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 权限类型
 * 
 * 系统模块提供权限持久化，各类型权限存储格式各自定义
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface IPermissionType {

	public String BEAN_PREFIX = "PermissionType_";
	
	/**
	 * 权限字符串分隔符
	 */
	public String Spliter = ":";
	
	/**
	 * 类型唯一标识
	 */
	public String getId();

	/**
	 * 类型名称
	 */
	public String getName();
	
	/**
	 * 将存储在sys_permission中的权限字符串解析成权限项列表
	 * 
	 * @param json
	 * @return
	 */
	default public List<String> parse(String json) {
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
