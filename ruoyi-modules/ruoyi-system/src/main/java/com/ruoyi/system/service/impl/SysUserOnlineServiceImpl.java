package com.ruoyi.system.service.impl;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.system.domain.SysUserOnline;
import com.ruoyi.system.service.ISysUserOnlineService;

/**
 * 在线用户 服务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysUserOnlineServiceImpl implements ISysUserOnlineService {

	/**
	 * 设置在线用户信息
	 * 
	 * @param loginUser
	 *            用户信息
	 * @return 在线用户
	 */
	@Override
	public SysUserOnline loginUserToUserOnline(LoginUser loginUser) {
		if (Objects.isNull(loginUser) || Objects.isNull(loginUser.getUser())) {
			return null;
		}
		SysUserOnline sysUserOnline = new SysUserOnline();
		sysUserOnline.setTokenId(loginUser.getToken());
		sysUserOnline.setUserName(loginUser.getUsername());
		sysUserOnline.setIpaddr(loginUser.getIpaddr());
		sysUserOnline.setLoginLocation(loginUser.getLoginLocation());
		sysUserOnline.setBrowser(loginUser.getBrowser());
		sysUserOnline.setOs(loginUser.getOs());
		sysUserOnline.setLoginTime(loginUser.getLoginTime());
		SysUser user = (SysUser) loginUser.getUser();
		sysUserOnline.setDeptName(user.getDeptName());
		return sysUserOnline;
	}
}
