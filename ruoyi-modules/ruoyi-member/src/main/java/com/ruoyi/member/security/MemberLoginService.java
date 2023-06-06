package com.ruoyi.member.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.security.SecurityUtils;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.security.enums.DeviceType;
import com.ruoyi.common.utils.IP2RegionUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.member.domain.Member;
import com.ruoyi.member.exception.MemberErrorCode;
import com.ruoyi.member.service.IMemberService;
import com.ruoyi.system.exception.SysErrorCode;
import com.ruoyi.system.fixed.dict.LoginLogType;
import com.ruoyi.system.fixed.dict.SuccessOrFail;
import com.ruoyi.system.fixed.dict.UserStatus;
import com.ruoyi.system.service.ISecurityConfigService;
import com.ruoyi.system.service.ISysLogininforService;

import cn.dev33.satoken.session.SaSession;
import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;

/**
 * 会员登录校验方法
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class MemberLoginService {
	
	private final ISysLogininforService logininfoService;

	private final IMemberService memberService;

	private final ISecurityConfigService securityConfigService;

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
		// 查找用户
		Member member = this.memberService.lambdaQuery().eq(Member::getUserName, username).one();
		if (Objects.isNull(member)) {
			throw MemberErrorCode.MEMBER_NOT_EXISTS.exception();
		}
		if (UserStatus.isDisbale(member.getStatus())) {
			throw MemberErrorCode.MEMBER_DISABLED.exception();
		}
		// 密码校验
		if (!SecurityUtils.matches(password, member.getPassword())) {
			// 密码错误处理策略
			this.securityConfigService.processLoginPasswordError(member);
			// 记录日志
			asyncTaskManager.execute(this.logininfoService.recordLogininfor(MemberUserType.TYPE, member.getMemberId(),
					member.getUserName(), LoginLogType.LOGIN, SuccessOrFail.FAIL, "Invalid password."));
			throw SysErrorCode.PASSWORD_ERROR.exception();
		}
		this.securityConfigService.onLoginSuccess(member);
		// 记录用户最近登录时间和ip地址
		member.setLastLoginIp(ServletUtils.getIpAddr(ServletUtils.getRequest()));
		member.setLastLoginTime(LocalDateTime.now());
		memberService.updateById(member);
		// 生成token
		LoginUser loginUser = createLoginUser(member);
		StpMemberUtil.login(member.getUserId(), DeviceType.PC.value());
		loginUser.setToken(StpMemberUtil.getTokenValueByLoginId(member.getUserId()));
		StpMemberUtil.getTokenSession().set(SaSession.USER, loginUser);
		// 日志
		asyncTaskManager.execute(this.logininfoService.recordLogininfor(MemberUserType.TYPE, member.getUserId(),
				loginUser.getUsername(), LoginLogType.LOGIN, SuccessOrFail.SUCCESS, StringUtils.EMPTY));
		return StpMemberUtil.getTokenValue();
	}

	private LoginUser createLoginUser(Member member) {
		LoginUser loginUser = new LoginUser();
		loginUser.setUserId(member.getMemberId());
		loginUser.setUserType(MemberUserType.TYPE);
		loginUser.setUsername(member.getUserName());
		loginUser.setLoginTime(Instant.now().toEpochMilli());
		loginUser.setLoginLocation(IP2RegionUtils.ip2Region(member.getLastLoginIp()));
		loginUser.setIpaddr(member.getLastLoginIp());
		UserAgent ua = UserAgent.parseUserAgentString(ServletUtils.getUserAgent());
		loginUser.setOs(ua.getOperatingSystem().name());
		loginUser.setBrowser(ua.getBrowser() + "/" + ua.getBrowserVersion());
		loginUser.setUser(member);
		return loginUser;
	}
}
