package com.ruoyi.common.security.domain;

import java.io.Serializable;
import java.util.Map;

import com.ruoyi.common.security.SecurityUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 登录用户身份权限
 */
@Getter
@Setter
public class LoginUser implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	private Long userId;
	
	/**
	 * 用户类型
	 */
	private String userType;

	/**
	 * 用户名
	 */
	private String username;
	
	/**
	 * 部门ID
	 */
	private Long deptId;

	/**
	 * 用户唯一标识
	 */
	private String token;

	/**
	 * 登录时间
	 */
	private Long loginTime;

	/**
	 * 过期时间
	 */
	private Long expireTime;

	/**
	 * 登录IP地址
	 */
	private String ipaddr;

	/**
	 * 登录地点
	 */
	private String loginLocation;

	/**
	 * 浏览器类型
	 */
	private String browser;

	/**
	 * 操作系统
	 */
	private String os;

	/**
	 * 权限列表
	 */
	private Map<String, String> permissions;

	/**
	 * 用户信息
	 */
	private Object user;
	
	public boolean isSuperAdministrator() {
		return SecurityUtils.isSuperAdmin(userId);
	}
}
