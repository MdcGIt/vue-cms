package com.ruoyi.contentcore.intercepter;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;

import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.security.StpAdminUtil;

import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.NotPermissionException;
import lombok.RequiredArgsConstructor;

/**
 * 站点/栏目/内容权限注解校验拦截
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Aspect
@Component
@RequiredArgsConstructor
public class CmsPrivAspect {

	private static final PropertyPlaceholderHelper PlaceholderHelper = new PropertyPlaceholderHelper("{", "}");

	@Before("@annotation(cmsPriv)")
	public void before(JoinPoint joinPoint, CmsPriv cmsPriv) {
		String[] values = cmsPriv.value();
		if (StringUtils.isNotEmpty(values) && StpAdminUtil.isLogin()) {
			Properties properties = new Properties();
			if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
				String[] argNames = methodSignature.getParameterNames();
				for (int i = 0; i < argNames.length; i++) {
					properties.put(argNames[i], joinPoint.getArgs()[i].toString());
				}
			}
			// TODO 扩展SpringEl支持类似{site.siteId}的对象属性占位符解析
			List<String> checkPerms = Stream.of(values).map(v -> PlaceholderHelper.replacePlaceholders(v, properties))
					.toList();
			if (cmsPriv.mode() == SaMode.AND) {
				checkAnd(checkPerms, StpAdminUtil.getLoginUser());
			} else {
				checkOr(checkPerms, StpAdminUtil.getLoginUser());
			}
		}
	}

	private void checkAnd(List<String> checkPerms, LoginUser loginUser) {
		Set<String> permissions = loginUser.getPermissions();
		for (String perm : checkPerms) {
			if (!permissions.contains(perm)) {
				throw new NotPermissionException(perm, loginUser.getUserType()).setCode(SaErrorCode.CODE_11051);
			}
		}
	}

	private void checkOr(List<String> checkPerms, LoginUser loginUser) {
		Set<String> permissions = loginUser.getPermissions();
		for (String perm : checkPerms) {
			if (permissions.contains(perm)) {
				return;
			}
		}
		throw new NotPermissionException(StringUtils.join(checkPerms), loginUser.getUserType())
				.setCode(SaErrorCode.CODE_11051);
	}
}
