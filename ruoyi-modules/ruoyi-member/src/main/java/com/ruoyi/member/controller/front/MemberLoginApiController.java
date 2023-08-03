package com.ruoyi.member.controller.front;

import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.security.SecurityUtils;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.member.domain.Member;
import com.ruoyi.member.domain.dto.*;
import com.ruoyi.member.domain.vo.MemberCache;
import com.ruoyi.member.domain.vo.MemberMenuVO;
import com.ruoyi.member.fixed.config.MemberResourcePrefix;
import com.ruoyi.member.security.MemberLoginService;
import com.ruoyi.member.security.MemberUserType;
import com.ruoyi.member.security.StpMemberUtil;
import com.ruoyi.member.service.IMemberService;
import com.ruoyi.member.service.IMemberStatDataService;
import com.ruoyi.member.util.MemberUtils;
import com.ruoyi.system.annotation.IgnoreDemoMode;
import com.ruoyi.system.fixed.dict.LoginLogType;
import com.ruoyi.system.fixed.dict.SuccessOrFail;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISecurityConfigService;
import com.ruoyi.system.service.ISysLogininforService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member")
public class MemberLoginApiController extends BaseRestController {

	private final IMemberService memberService;

	private final ISecurityConfigService securityConfigService;

	private final MemberLoginService memberLoginService;

	private final IMemberStatDataService memberStatDataService;

	private final ISysLogininforService logininforService;

	private final AsyncTaskManager asyncTaskManager;

	private final JavaMailSender javaMailSender;

	private final RedisCache redisCache;

	private static final String SMS_CODE_CACHE_PREFIX = "member:sms_code:";

	@Value("${spring.mail.username:}")
	private String mailSendUser;

	@GetMapping("/is_login")
	public R<?> checkLogin(@RequestParam(required = false, defaultValue = "false") Boolean preview) {
		boolean login = StpMemberUtil.isLogin();
		if (!login) {
			return R.fail("Not Login");
		}
		MemberCache memberCache = memberStatDataService.getMemberCache(StpMemberUtil.getLoginIdAsLong());
		if (StringUtils.isNotEmpty(memberCache.getAvatar())) {
			memberCache.setAvatar(MemberUtils.getMemberResourcePrefix(preview) + memberCache.getAvatar());
		}
		if (StringUtils.isNotEmpty(memberCache.getCover())) {
			memberCache.setAvatar(MemberUtils.getMemberResourcePrefix(preview) + memberCache.getCover());
		}
		if (memberCache.getMenus().isEmpty()) {
			memberCache.getMenus().add(new MemberMenuVO("账号信息", "account/setting"));
			memberCache.getMenus().add(new MemberMenuVO("修改密码", "account/password"));
			memberCache.getMenus().add(new MemberMenuVO("文章投稿", "account/contribute"));
		}
		return R.ok(memberCache);
	}

	@IgnoreDemoMode
	@PostMapping("/login")
	public R<?> login(@RequestBody MemberLoginDTO dto) {
		dto.setUserAgent(ServletUtils.getUserAgent());
		dto.setIp(ServletUtils.getIpAddr(ServletUtils.getRequest()));
		String token = memberLoginService.login(dto);
		return R.ok(token);
	}

	@IgnoreDemoMode
	@PostMapping("/register")
	public R<?> resgiter(@RequestBody MemberRegisterDTO dto) {
		dto.setUserAgent(ServletUtils.getUserAgent());
		dto.setIp(ServletUtils.getIpAddr(ServletUtils.getRequest()));
		String token = this.memberLoginService.register(dto);
		return R.ok(token);
	}

	@IgnoreDemoMode
	@PostMapping("/logout")
	public R<?> logout() {
		try {
			if (StpMemberUtil.isLogin()) {
				LoginUser loginUser = StpMemberUtil.getLoginUser();
				StpMemberUtil.logout();
				asyncTaskManager.execute(this.logininforService.recordLogininfor(loginUser.getUserType(),
						loginUser.getUserId(), loginUser.getUsername(), LoginLogType.LOGOUT, SuccessOrFail.SUCCESS,
						StringUtils.EMPTY));
			}
		} catch (Exception e) {
		}
		return R.ok();
	}

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PutMapping("/reset_pwd")
	public R<?> resetMemberPassword(@RequestBody @Validated ResetMemberPasswordDTO dto) {
		Member member = this.memberService.getById(StpMemberUtil.getLoginIdAsLong());
		if (!SecurityUtils.matches(dto.getPassword(), member.getPassword())) {
			return R.fail("密码错错误");
		}
		// 密码规则校验
		this.securityConfigService.validPassword(member, dto.getNewPassword());

		boolean update = this.memberService.lambdaUpdate()
				.set(Member::getPassword, SecurityUtils.passwordEncode(dto.getNewPassword()))
				.eq(Member::getMemberId, member.getMemberId())
				.update();
		return update ? R.ok() : R.fail();
	}

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/sms_code")
	public R<?> sendSmsCode(@RequestParam(required = false, defaultValue = "email") String type) {
		if (StringUtils.isEmpty(mailSendUser)) {
			return R.fail("发送失败，请联系管理员！");
		}
		try {
			Member member = this.memberService.getById(StpMemberUtil.getLoginIdAsLong());

			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(mailSendUser);
			message.setTo(member.getEmail());
			message.setSubject("邮箱绑定验证");
			String code = String.valueOf(RandomUtils.nextInt(100000, 999999));
			message.setText("验证码：" + code);
			javaMailSender.send(message);

			redisCache.setCacheObject(SMS_CODE_CACHE_PREFIX + member.getMemberId(), code, 600, TimeUnit.SECONDS);
			return R.ok();
		} catch (Exception e) {
			return R.fail("发送失败，请联系管理员！");
		}
	}

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PutMapping("/change_email")
	public R<?> changeMemberEmail(@RequestBody @Validated ChangeMemberEmailDTO dto) {
		String authCode = this.redisCache.getCacheObject(SMS_CODE_CACHE_PREFIX + StpMemberUtil.getLoginIdAsLong());
		if (StringUtils.isEmpty(authCode) || !dto.getAuthCode().equals(authCode)) {
			return R.fail("验证码错误");
		}

		Member member = this.memberService.getById(StpMemberUtil.getLoginIdAsLong());
		boolean update = this.memberService.lambdaUpdate()
				.set(Member::getEmail, dto.getEmail())
				.eq(Member::getMemberId, member.getMemberId())
				.update();
		return update ? R.ok() : R.fail();
	}

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PutMapping("/info")
	public R<?> saveMemberInfo(@RequestBody @Validated MemberInfoDTO dto) {
		LoginUser loginUser = StpMemberUtil.getLoginUser();
		Member member = (Member) loginUser.getUser();

		boolean update = this.memberService.lambdaUpdate().set(Member::getNickName, dto.getNickName())
				.set(Member::getSlogan, dto.getSlogan())
				.set(Member::getDescription, dto.getDescription())
				.eq(Member::getMemberId, member.getMemberId())
				.update();
		if (!update) {
			return R.fail();
		}
		member.setNickName(dto.getNickName());
		member.setSlogan(dto.getSlogan());
		member.setDescription(dto.getDescription());
		StpAdminUtil.setLoginUser(loginUser);

		this.memberStatDataService.removeMemberCache(member.getMemberId());
		return R.ok();
	}

	@IgnoreDemoMode
	@Priv(type = MemberUserType.TYPE)
	@PostMapping("/avatar")
	public R<?> uploadMemberAvatar(@RequestParam String image,
								   @RequestParam(required = false, defaultValue = "false") Boolean preview) throws IOException {
		String url = this.memberService.uploadAvatarByBase64(StpMemberUtil.getLoginIdAsLong(), image);

		this.memberStatDataService.removeMemberCache(StpMemberUtil.getLoginIdAsLong());
		return R.ok(MemberUtils.getMemberResourcePrefix(preview) + url);
	}
}