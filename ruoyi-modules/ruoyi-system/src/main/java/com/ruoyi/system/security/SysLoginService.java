package com.ruoyi.system.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.security.SecurityUtils;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.security.enums.DeviceType;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IP2RegionUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.SysConstants;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.system.exception.SysErrorCode;
import com.ruoyi.system.fixed.config.SysCaptchaEnable;
import com.ruoyi.system.fixed.dict.LoginLogType;
import com.ruoyi.system.fixed.dict.SuccessOrFail;
import com.ruoyi.system.fixed.dict.UserStatus;
import com.ruoyi.system.service.ISecurityConfigService;
import com.ruoyi.system.service.ISysDeptService;
import com.ruoyi.system.service.ISysLogininforService;
import com.ruoyi.system.service.ISysPermissionService;
import com.ruoyi.system.service.ISysUserService;

import cn.dev33.satoken.session.SaSession;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;

/**
 * 登录校验方法
 * 
 * @author ruoyi
 */
@Component
@RequiredArgsConstructor
public class SysLoginService {

	private final RedisCache redisCache;

	private final ISysDeptService deptService;

	private final ISysUserService userService;

	private final ISysLogininforService logininfoService;

	private final ISecurityConfigService securityConfigService;

	private final ISysPermissionService permissionService;

	private final AsyncTaskManager asyncTaskManager;

	/**
	 * 登录验证
	 * 
	 * @param username 用户名
	 * @param password 密码
	 * @param code     验证码
	 * @param uuid     唯一标识
	 * @return 结果
	 */
	public String login(String username, String password, String code, String uuid) {
		// 验证码开关
		if (SysCaptchaEnable.isEnable()) {
			validateCaptcha(username, code, uuid);
		}
		// 查找用户
		SysUser user = this.userService.lambdaQuery().eq(SysUser::getUserName, username).one();
		if (Objects.isNull(user)) {
			throw SysErrorCode.USER_NOT_EXISTS.exception();
		}
		if (!user.isAccountNonLocked()) {
			throw SysErrorCode.USER_LOCKED
					.exception(Objects.isNull(user.getLockEndTime()) ? "forever" : user.getLockEndTime().toString());
		} else if (user.getStatus() == UserStatus.DISABLE) {
			throw SysErrorCode.USER_DISABLED.exception();
		}
		// 密码校验
		if (!SecurityUtils.matches(password, user.getPassword())) {
			// 密码错误处理策略
			this.securityConfigService.processLoginPasswordError(user);
			if (user.isModified()) {
				this.userService.updateById(user);
			}
			// 记录日志
			asyncTaskManager.execute(this.logininfoService.recordLogininfor(AdminUserType.TYPE, user.getUserId(),
					user.getUserName(), LoginLogType.LOGIN, SuccessOrFail.FAIL, "Invalid password."));
			throw SysErrorCode.PASSWORD_ERROR.exception();
		}
		this.securityConfigService.onLoginSuccess(user);
		// 记录用户最近登录时间和ip地址
		user.setLoginIp(ServletUtils.getIpAddr(ServletUtils.getRequest()));
		user.setLoginDate(LocalDateTime.now());
		userService.updateById(user);
		deptService.getDept(user.getDeptId()).ifPresent(d -> user.setDeptName(d.getDeptName()));
		// 生成token
		LoginUser loginUser = createLoginUser(user);
		StpAdminUtil.login(user.getUserId(), DeviceType.PC.value());
		loginUser.setToken(StpAdminUtil.getTokenValueByLoginId(user.getUserId()));
		StpAdminUtil.getTokenSession().set(SaSession.USER, loginUser);
		// 日志
		asyncTaskManager.execute(this.logininfoService.recordLogininfor(AdminUserType.TYPE, user.getUserId(),
				loginUser.getUsername(), LoginLogType.LOGIN, SuccessOrFail.SUCCESS, StringUtils.EMPTY));
		return StpAdminUtil.getTokenValue();
	}

	private LoginUser createLoginUser(SysUser user) {
		LoginUser loginUser = new LoginUser();
		loginUser.setUserId(user.getUserId());
		loginUser.setDeptId(user.getDeptId());
		loginUser.setUserType(AdminUserType.TYPE);
		loginUser.setUsername(user.getUserName());
		loginUser.setLoginTime(Instant.now().toEpochMilli());
		loginUser.setLoginLocation(IP2RegionUtils.ip2Region(user.getLoginIp()));
		loginUser.setIpaddr(user.getLoginIp());
		UserAgent ua = UserAgent.parseUserAgentString(ServletUtils.getUserAgent());
		loginUser.setOs(ua.getOperatingSystem().name());
		;
		loginUser.setBrowser(ua.getBrowser() + "/" + ua.getBrowserVersion());
		loginUser.setUser(user);

		List<String> permissions = permissionService.getPermissionListByUser(user.getUserId());
		loginUser.setPermissions(permissions);
		return loginUser;
	}

	/**
	 * 校验验证码
	 * 
	 * @param username 用户名
	 * @param code     验证码
	 * @param uuid     唯一标识
	 * @return 结果
	 */
	public void validateCaptcha(String username, String code, String uuid) {
		Assert.notEmpty(uuid, () -> SysErrorCode.CAPTCHA_ERR.exception());

		String verifyKey = SysConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, StringUtils.EMPTY);
		String captcha = redisCache.getCacheObject(verifyKey);
		// 过期判断
		Assert.notNull(captcha, () -> {
			asyncTaskManager.execute(this.logininfoService.recordLogininfor(AdminUserType.TYPE, null, username,
					LoginLogType.LOGIN, SuccessOrFail.FAIL, SysErrorCode.CAPTCHA_EXPIRED.name()));
			return SysErrorCode.CAPTCHA_EXPIRED.exception();
		});
		// 未过期移除缓存
		redisCache.deleteObject(verifyKey);
		// 判断是否与输入验证码一致
		Assert.isTrue(StringUtils.equals(code, captcha), () -> {
			asyncTaskManager.execute(this.logininfoService.recordLogininfor(AdminUserType.TYPE, null, username,
					LoginLogType.LOGIN, SuccessOrFail.FAIL, SysErrorCode.CAPTCHA_ERR.name()));
			return SysErrorCode.CAPTCHA_ERR.exception();
		});
	}
}
