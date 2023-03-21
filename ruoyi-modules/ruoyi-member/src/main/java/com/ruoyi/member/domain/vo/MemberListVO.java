package com.ruoyi.member.domain.vo;

import java.time.LocalDate;

import com.ruoyi.common.security.domain.BaseDTO;

import lombok.Getter;
import lombok.Setter;

/***
 * 会员表
 */
@Getter
@Setter
public class MemberListVO extends BaseDTO {
	
	/**
	 * 会员ID
	 */
	private Long memberId;

	/**
	 * 会员用户名
	 */
	private String userName;
	
	/**
	 * 昵称
	 */
	private String nickName;
	
	/**
	 * 手机号
	 */
	private String phoneNumber;
	
	/**
	 * Email
	 */
	private String email;
	
	/**
	 * 出生日期
	 */
	private LocalDate birthday;
	
	/**
	 * 状态
	 */
	private String status; 
	
	/**
	 * 来源类型
	 */
	private String sourceType;
	
	/**
	 * 来源ID
	 */
	private String sourceId;
	
	/**
	 * 最近登录IP
	 */
	private String lastLoginIp;
	
	/**
	 * 最近登录时间
	 */
	private String lastLoginTime;
}
