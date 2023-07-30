package com.ruoyi.system.controller;

import com.ruoyi.common.config.properties.RuoYiProperties;
import com.ruoyi.system.config.properties.SysProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.system.domain.vo.server.Server;
import com.ruoyi.system.permission.SysMenuPriv;
import com.ruoyi.system.security.AdminUserType;

/**
 * 服务器监控
 * 
 * @author ruoyi
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/server")
public class ServerController {

	private final RuoYiProperties properties;
	
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorServerList)
	@GetMapping()
	public R<?> getInfo() throws Exception {
		Server server = new Server();
		server.copyTo();
		server.getApp().setName(properties.getName());
		server.getApp().setVersion(properties.getVersion());
		return R.ok(server);
	}
}
