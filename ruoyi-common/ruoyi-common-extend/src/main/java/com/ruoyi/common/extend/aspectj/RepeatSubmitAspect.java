package com.ruoyi.common.extend.aspectj;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

import com.ruoyi.common.extend.ExtendConstants;
import com.ruoyi.common.extend.annotation.RepeatSubmit;
import com.ruoyi.common.extend.exception.RepeatSubmitErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.security.IUserType;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.StpLogic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 防重复提交，仅对已登录用户生效
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class RepeatSubmitAspect {

	private final RedisCache redisCache;

	private final List<IUserType> userTypes;

	@Before("@annotation(repeatSubmit)")
	public void doBefore(JoinPoint point, RepeatSubmit repeatSubmit) throws Throwable {
		// PREFIX + CLASS.METHOD + TOKEN
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		Class<?> targetClass = method.getDeclaringClass();
		StringBuffer sb = new StringBuffer();
		sb.append(ExtendConstants.REPEAT_SUBMIT_KEY).append(targetClass.getName()).append(".").append(method.getName())
				.append(".");
		// 获取用户token作为缓存key组成部分
		String token = null;
		for (IUserType ut : userTypes) {
			StpLogic stpLogic = SaManager.getStpLogic(ut.getType());
			if (stpLogic.isLogin()) {
				token = stpLogic.getTokenValue();
				break;
			}
		}
		if (StringUtils.isEmpty(token)) {
			log.warn("Method '{0}' annotation by @RepeatSubmit, but no @Priv/@SaCheckLogin.", sb.toString());
			return;
		}
		sb.append(token);
		String cacheKey = sb.toString();
		if (this.redisCache.hasKey(cacheKey)) {
			throw RepeatSubmitErrorCode.REPEAT_ERR.exception();
		}
		this.redisCache.setCacheObject(cacheKey, 1, repeatSubmit.interval(), TimeUnit.MILLISECONDS);
	}
}
