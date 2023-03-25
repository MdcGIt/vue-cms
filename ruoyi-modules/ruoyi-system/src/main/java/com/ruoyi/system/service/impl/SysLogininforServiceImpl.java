package com.ruoyi.system.service.impl;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.utils.ConvertUtils;
import com.ruoyi.common.utils.IP2RegionUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysLogininfor;
import com.ruoyi.system.mapper.SysLogininforMapper;
import com.ruoyi.system.service.ISysLogininforService;

import eu.bitwalker.useragentutils.UserAgent;
import lombok.RequiredArgsConstructor;

/**
 * 系统访问日志情况信息 服务层处理
 * 
 * @author ruoyi
 */
@Service
@RequiredArgsConstructor
public class SysLogininforServiceImpl extends ServiceImpl<SysLogininforMapper, SysLogininfor>
		implements ISysLogininforService {

	private static final Logger sys_user_logger = LoggerFactory.getLogger("sys-user");

	private final SysLogininforMapper logininforMapper;

	/**
	 * 清空系统登录日志
	 */
	@Override
	public void cleanLogininfor() {
		logininforMapper.cleanLogininfor();
	}
	
	@Override
	public AsyncTask recordLogininfor(String userType, Object userId, String username, String logType, String status, String message, Object... args) {
		final UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getUserAgent());
		final String ip = ServletUtils.getIpAddr(ServletUtils.getRequest());
		return new AsyncTask() {
			
			@Override
			public String getType() {
				return "LoginLog";
			}
			
			@Override
			public void run0() {
				// 打印信息到日志
				StringBuilder s = new StringBuilder();
				s.append("[").append(ip).append("]");
				s.append("[type:").append(userType).append("]");
				s.append("[uid:").append(userId).append("]");
				s.append("[uname:").append(username).append("]");
				s.append("[").append(logType).append("]");
				s.append("[").append(status).append("]");
				s.append("[").append(message).append("]");
				sys_user_logger.info(s.toString(), args);
				// 获取客户端操作系统
				String os = userAgent.getOperatingSystem().getName();
				// 获取客户端浏览器
				String browser = userAgent.getBrowser().getName();
				// 封装对象
				SysLogininfor logininfor = new SysLogininfor();
				logininfor.setUserType(userType);
				logininfor.setUserId(ConvertUtils.toStr(userId));
				logininfor.setUserName(username);
				logininfor.setLoginTime(LocalDateTime.now());
				logininfor.setIpaddr(ip);
				logininfor.setLoginLocation(IP2RegionUtils.ip2Region(ip));
				logininfor.setBrowser(browser);
				logininfor.setOs(os);
				if (StringUtils.isNotBlank(message)) {
					logininfor.setMsg(message.length() > 2000 ? message.substring(0,  2000) : message);
				}
				logininfor.setLogType(logType);
				// 日志状态
				logininfor.setStatus(status);
				save(logininfor);
			}
		};
	}
}
