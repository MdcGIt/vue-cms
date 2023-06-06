package com.ruoyi.system.security;

import java.time.LocalDateTime;

/**
 * 安全配置应用对象接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISecurityUser {

	/**
	 * 用户类型
	 */
	public String getType();

	/**
	 * 用户ID
	 */
	public Long getUserId();

	/**
	 * 用户名
	 */
	public String getUserName();

	/**
	 * 手机号
	 */
	public String getPhonenumber();

	/**
	 * Email
	 */
	public String getEmail();

	/**
	 * 昵称
	 */
	public String getNickName();

	/**
	 * 真实姓名
	 */
	public String getRealName();

	/**
	 * 出生日期
	 */
	public LocalDateTime getBirthday();

	/**
	 * 封禁用户
	 * 
	 * @param status
	 */
	public void disbaleUser();

	/**
	 * 锁定用户
	 * 
	 * @param lockEndTime
	 */
	public void lockUser(LocalDateTime lockEndTime);

	/**
	 * 强制登录后修改密码
	 */
	public void forceModifyPassword();

}
