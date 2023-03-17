package com.ruoyi.common.security;

import cn.dev33.satoken.secure.BCrypt;

/**
 * 安全服务工具类
 */
public class SecurityUtils {
	
	/**
	 * 密码加密
	 * 
	 * @param password
	 * @return
	 */
	public static String passwordEncode(String password) {
		return BCrypt.hashpw(password);
	}

	/**
	 * 校验密码
	 * 
	 * @param rawPassword
	 * @param encodedPassword
	 * @return
	 */
	public static boolean matches(String password, String encodedPassword) {
		return BCrypt.checkpw(password, encodedPassword);
	}
}
