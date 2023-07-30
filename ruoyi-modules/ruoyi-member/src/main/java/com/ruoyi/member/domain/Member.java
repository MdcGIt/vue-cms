package com.ruoyi.member.domain;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.db.domain.BaseEntity;
import com.ruoyi.member.fixed.dict.MemberStatus;
import com.ruoyi.member.security.MemberUserType;
import com.ruoyi.system.security.ISecurityUser;

import lombok.Getter;
import lombok.Setter;

/***
 * 会员表
 */
@Getter
@Setter
@TableName(Member.TABLE_NAME)
public class Member extends BaseEntity implements ISecurityUser {

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
	private String avatar;

	/**
	 * 个人主页封面图
	 */
	private String cover;

	/**
	 * 个性签名
	 */
	private String slogan;

	/**
	 * 个人简介
	 */
	private String description;
	
	/**
	 * 手机号
	 */
	private String phonenumber;
	
	/**
	 * Email
	 */
	private String email;
	
	/**
	 * 出生日期
	 */
	private LocalDateTime birthday;
	
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
	private LocalDateTime lastLoginTime;
	
	/**
	 * 数据变更标识
	 */
	@TableField(exist = false)
	private boolean modified;

	@Override
	public String getType() {
		return MemberUserType.TYPE;
	}

	@Override
	public Long getUserId() {
		return this.memberId;
	}

	@Override
	public String getRealName() {
		return null;
	}

	@Override
	public void disbaleUser() {
		this.status = MemberStatus.DISABLE;
	}

	@Override
	public void lockUser(LocalDateTime lockEndTime) {
		this.status = MemberStatus.LOCK;
	}

	@Override
	public void forceModifyPassword() {
	}
}
