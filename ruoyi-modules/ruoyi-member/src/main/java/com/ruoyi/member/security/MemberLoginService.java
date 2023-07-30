package com.ruoyi.member.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.exception.GlobalException;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.member.domain.dto.MemberLoginDTO;
import com.ruoyi.member.domain.dto.MemberRegisterDTO;
import com.ruoyi.member.fixed.dict.MemberStatus;
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
	 * @return 结果
	 */
	public String login(MemberLoginDTO dto) {
		// 查找用户
		Member member = this.memberService.lambdaQuery()
				.eq(Member::getUserName, dto.getUsername())
				.or().eq(Member::getEmail, dto.getUsername())
				.or().eq(Member::getPhonenumber, dto.getUsername())
				.one();
		if (Objects.isNull(member)) {
			throw MemberErrorCode.MEMBER_NOT_EXISTS.exception();
		}
		if (UserStatus.isDisbale(member.getStatus())) {
			throw MemberErrorCode.MEMBER_DISABLED.exception();
		}
		// 密码校验
		if (!SecurityUtils.matches(dto.getPassword(), member.getPassword())) {
			// 密码错误处理策略
			this.securityConfigService.processLoginPasswordError(member);
			// 记录日志
			asyncTaskManager.execute(this.logininfoService.recordLogininfor(MemberUserType.TYPE, member.getMemberId(),
					member.getUserName(), LoginLogType.LOGIN, SuccessOrFail.FAIL, "Invalid password."));
			throw SysErrorCode.PASSWORD_ERROR.exception();
		}
		this.securityConfigService.onLoginSuccess(member);
		// 记录用户最近登录时间和ip地址
		member.setLastLoginIp(dto.getIp());
		member.setLastLoginTime(LocalDateTime.now());
		memberService.updateById(member);
		// 生成token
		LoginUser loginUser = createLoginUser(member, dto.getUserAgent());
		StpMemberUtil.login(member.getUserId(), DeviceType.PC.value());
		loginUser.setToken(StpMemberUtil.getTokenValueByLoginId(member.getUserId()));
		StpMemberUtil.getTokenSession().set(SaSession.USER, loginUser);
		// 日志
		asyncTaskManager.execute(this.logininfoService.recordLogininfor(MemberUserType.TYPE, member.getUserId(),
				loginUser.getUsername(), LoginLogType.LOGIN, SuccessOrFail.SUCCESS, StringUtils.EMPTY));
		return StpMemberUtil.getTokenValue();
	}

	private LoginUser createLoginUser(Member member, String userAgent) {
		LoginUser loginUser = new LoginUser();
		loginUser.setUserId(member.getMemberId());
		loginUser.setUserType(MemberUserType.TYPE);
		loginUser.setUsername(member.getUserName());
		loginUser.setLoginTime(Instant.now().toEpochMilli());
		loginUser.setLoginLocation(IP2RegionUtils.ip2Region(member.getLastLoginIp()));
		loginUser.setIpaddr(member.getLastLoginIp());
		UserAgent ua = UserAgent.parseUserAgentString(userAgent);
		loginUser.setOs(ua.getOperatingSystem().name());
		loginUser.setBrowser(ua.getBrowser() + "/" + ua.getBrowserVersion());
		loginUser.setUser(member);
		return loginUser;
	}

	public String register(MemberRegisterDTO dto) {
		// 查找用户
		LambdaQueryWrapper<Member> q = new LambdaQueryWrapper<>();
		if ("email".equals(dto.getType())) {
			q.eq(Member::getEmail, dto.getUserName());
		} else if ("phone".equals(dto.getType())) {
			q.eq(Member::getPhonenumber, dto.getUserName());
		} else {
			q.eq(Member::getUserName, dto.getUserName());
		}
		Member member = this.memberService.getOne(q);
		if (Objects.nonNull(member)) {
			throw new GlobalException("账号已存在");
		}
		// 密码规则校验
		this.securityConfigService.validPassword(null, dto.getPassword());

		member = new Member();
		member.setMemberId(IdUtils.getSnowflakeId());
		// TODO 抽象注册类型
		if ("email".equals(dto.getType())) {
			member.setEmail(dto.getUserName());
			member.setUserName(member.getMemberId().toString());
		} else if ("phone".equals(dto.getType())) {
			member.setPhonenumber(dto.getUserName());
			member.setUserName(member.getMemberId().toString());
		} else {
			member.setUserName(dto.getUserName());
		}
		member.setPassword(SecurityUtils.passwordEncode(dto.getPassword()));
		member.setStatus(MemberStatus.ENABLE);
		// 记录用户最近登录时间和ip地址
		member.setLastLoginIp(dto.getIp());
		member.setLastLoginTime(LocalDateTime.now());
		member.createBy(member.getUserName());
		this.memberService.save(member);

		// 生成token
		LoginUser loginUser = createLoginUser(member, dto.getUserAgent());
		StpMemberUtil.login(member.getUserId(), DeviceType.PC.value());
		loginUser.setToken(StpMemberUtil.getTokenValueByLoginId(member.getUserId()));
		StpMemberUtil.getTokenSession().set(SaSession.USER, loginUser);
		return StpMemberUtil.getTokenValue();
	}
}
