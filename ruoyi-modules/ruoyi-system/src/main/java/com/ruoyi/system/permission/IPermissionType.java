package com.ruoyi.system.permission;

import cn.dev33.satoken.annotation.SaMode;

import java.util.List;
import java.util.Set;

/**
 * 权限类型
 * 
 * 系统模块提供权限持久化，各类型权限存储格式各自定义
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IPermissionType<T> {

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
	 * 将存储在sys_permission中的权限字符串解析成权限项集合
	 * 
	 * @param json
	 * @return
	 */
	public T deserialize(String json);

	/**
	 * 转权限项列表转成持久化存储字符串
	 * 
	 * @param permissionKeys
	 * @return
	 */
	public String serialize(T permissionKeys);
	
	/**
	 * 合并权限项
	 * 
	 * @param permissionJsonList
	 * @return
	 */
	public String merge(List<String> permissionJsonList);

	/**
	 * 将数据库存储的权限数据转换成权限唯一标识的集合。
	 *
	 * 实现此方法的权限项集合会加入SA-TOKEN的权限列表中。
	 * @see com.ruoyi.system.security.AdminUserType#getPermissionList
	 *
	 * @param json
	 * @return
	 */
	default Set<String> convert(String json) {
		return Set.of();
	}
	
	/**
	 * 是否有权限
	 * 
	 * @param permissionKeys
	 * @param json
	 * @param mode
	 * @return
	 */
	public boolean hasPermission(List<String> permissionKeys, String json, SaMode mode);
}
