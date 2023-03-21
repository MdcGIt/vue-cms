package com.ruoyi.member.domain;

import java.time.LocalDate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

/***
 * 会员表
 */
@Getter
@Setter
@TableName(Member.TABLE_NAME)
public class Member extends BaseEntity {

	private static final long serialVersionUID = 1L;
	
	public static final String TABLE_NAME = "cc_member";

	@TableId(value = "member_id", type = IdType.INPUT)
    private Long memberId;
	
	/**
	 * 会员用户名
	 */
	private String userName;
	
	/**
	 * 会员密码
	 */
	private String password;
	
	/**
	 * 昵称
	 */
	private String nickName;
	
	/**
	 * 头像
	 */
	private Integer avatar; 
	
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
