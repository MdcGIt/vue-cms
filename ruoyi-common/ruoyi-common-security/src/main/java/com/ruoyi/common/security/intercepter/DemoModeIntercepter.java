package com.ruoyi.common.security.intercepter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.ruoyi.common.security.exception.SecurityErrorCode;

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
		if (HttpMethod.POST.matches(method) || HttpMethod.PUT.matches(method) || HttpMethod.DELETE.matches(method)) {
			throw SecurityErrorCode.DEMO_EXCEPTION.exception();
		}
		return true;
	}
}
