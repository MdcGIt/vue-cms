package com.ruoyi.member.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.dev33.satoken.annotation.SaCheckLogin;

@SaCheckLogin(type = MemberUserType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface SaMemberCheckLogin {

}