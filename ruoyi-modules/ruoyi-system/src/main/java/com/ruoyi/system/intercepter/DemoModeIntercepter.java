package com.ruoyi.system.intercepter;

import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ruoyi.common.Constants;
import com.ruoyi.common.security.exception.SecurityErrorCode;
import com.ruoyi.system.security.StpAdminUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 演示模式拦截器
 */
@RequiredArgsConstructor
public class DemoModeIntercepter implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String method = request.getMethod();
		if (StpAdminUtil.isLogin() && StpAdminUtil.getLoginIdAsLong() == Constants.SUPER_ADMIN) {
			return true; // 超级管理员允许操作
		}
		if (HttpMethod.POST.matches(method) || HttpMethod.PUT.matches(method) || HttpMethod.DELETE.matches(method)) {
			throw SecurityErrorCode.DEMO_EXCEPTION.exception();
		}
		return true;
	}
}
