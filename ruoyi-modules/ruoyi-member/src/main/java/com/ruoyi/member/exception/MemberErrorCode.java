package com.ruoyi.member.exception;

import com.ruoyi.common.exception.ErrorCode;

public enum MemberErrorCode implements ErrorCode {
	
	/**
	 * 不支持的等级类型：{0}
	 */
	UNSUPPORTED_LEVEL_TYPE,
	
	/**
	 * 等级配置“{0}”已存在
	 */
	LEVEL_CONFIG_EXIST,
	
	/**
	 * 已完成签到
	 */
	SIGN_IN_COMPLETED,
	
	/**
	 * 经验配置“{0}”已存在
	 */
	EXP_CONFIG_EXIST,
	
	/**
	 * 不支持的经验值操作项：{0}
	 */
	UNSUPPORTED_EXP_OP,
	
	/**
	 * 用户名冲突
	 */
	USERNAME_CONFLICT,
	
	/**
	 * 手机号冲突
	 */
	PHONE_CONFLICT,
	
	/**
	 * Email冲突
	 */
	EMAIL_CONFLICT,
	
	/**
	 * 用户名/手机号/Email不能全为空。
	 */
	USERNAME_PHONE_EMAIL_ALL_EMPTY;
	
	@Override
	public String value() {
		return "ERRCODE.MEMBER." + this.name();
	}
}
