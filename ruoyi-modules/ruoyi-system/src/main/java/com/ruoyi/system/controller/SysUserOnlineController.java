package com.ruoyi.system.controller;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysUserOnline;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysUserOnlineService;

import cn.dev33.satoken.session.SaSession;
import lombok.RequiredArgsConstructor;

/**
 * 在线用户监控
 * 
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseRestController {

	private final ISysUserOnlineService userOnlineService;

	private final RedisCache redisCache;
	
	@Priv(type = AdminUserType.TYPE, value = "monitor:online:list")
	@GetMapping("/list")
	public R<?> list(String ipaddr, String userName) {
		String keyPrefix = StpAdminUtil.getStpLogic().getConfig().getTokenName() + ":" + AdminUserType.TYPE + ":token:";
		Set<String> keys = redisCache.scanKeys(keyPrefix + "*", 100);
		List<SysUserOnline> userOnlineList = keys.stream().map(key -> {
			SaSession tokenSession = StpAdminUtil.getTokenSessionByToken(key.substring(key.lastIndexOf(":") + 1));
			LoginUser loginUser = tokenSession.getModel(SaSession.USER, LoginUser.class);
			return userOnlineService.loginUserToUserOnline(loginUser);
		}).filter(online -> {
			if (Objects.isNull(online)) {
				return false;
			}
			if(StringUtils.isNotEmpty(ipaddr) && !StringUtils.equals(ipaddr, online.getIpaddr())) {
				return false;
			}
			if(StringUtils.isNotEmpty(userName) && !StringUtils.equals(userName, online.getUserName())) {
				return false;
			}
			return true;
		}).sorted((u1, u2) -> u1.getLoginTime() > u2.getLoginTime() ? 1 : -1).toList();
		return bindDataTable(userOnlineList);
	}

	/**
	 * 强退用户
	 */
	@Priv(type = AdminUserType.TYPE, value = "monitor:online:forceLogout")
	@Log(title = "在线用户", businessType = BusinessType.FORCE)
	@DeleteMapping("/{tokenId}")
	public R<?> forceLogout(@PathVariable String tokenId) {
		StpAdminUtil.logoutByTokenValue(tokenId);
		return R.ok();
	}
}
