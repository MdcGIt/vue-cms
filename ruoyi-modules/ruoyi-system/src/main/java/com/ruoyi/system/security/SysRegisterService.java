package com.ruoyi.system.security;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.SysConstants;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.system.domain.dto.RegisterBody;
import com.ruoyi.system.exception.SysErrorCode;
import com.ruoyi.system.fixed.config.SysCaptchaEnable;
import com.ruoyi.system.fixed.config.SysRegistEnable;
import com.ruoyi.system.fixed.dict.LoginLogType;
import com.ruoyi.system.fixed.dict.SuccessOrFail;
import com.ruoyi.system.service.ISysLogininforService;
import com.ruoyi.system.service.ISysUserService;

import lombok.RequiredArgsConstructor;

/**
 * 注册校验方法
 * 
 * @author ruoyi
 */
@Component
@RequiredArgsConstructor
public class SysRegisterService {

	private final ISysUserService userService;

	private final RedisCache redisCache;

	private final ISysLogininforService logininfoService;

	private final AsyncTaskManager asyncTaskManager;

	/**
	 * 注册
	 */
	public void register(RegisterBody registerBody) {
		Assert.isTrue(SysRegistEnable.isEnable(), SysErrorCode.REGIST_DISABELD::exception);
		// 验证码开关
		if (SysCaptchaEnable.isEnable()) {
			validateCaptcha(registerBody.getUsername(), registerBody.getCode(), registerBody.getUuid());
		}
		SysUser user = new SysUser();
		user.setUserName(registerBody.getUsername());
		user.setPassword(registerBody.getPassword());
		user.setNickName(registerBody.getUsername());
		userService.registerUser(user);
		asyncTaskManager.execute(this.logininfoService.recordLogininfor(AdminUserType.TYPE, user.getUserId(),
				user.getUserName(), LoginLogType.REGIST, SuccessOrFail.SUCCESS, StringUtils.EMPTY));
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
		if (StringUtils.isBlank(uuid)) {
			throw SysErrorCode.CAPTCHA_ERR.exception();
		}
		String verifyKey = SysConstants.CAPTCHA_CODE_KEY + uuid;
		try {
			String captcha = redisCache.getCacheObject(verifyKey);
			if (Objects.isNull(captcha)) {
				throw SysErrorCode.CAPTCHA_EXPIRED.exception();
			}
			if (StringUtils.equalsIgnoreCase(code, captcha)) {
				throw SysErrorCode.CAPTCHA_ERR.exception();
			}
		} finally {
			redisCache.deleteObject(verifyKey);
		}
	}
}
