package com.ruoyi.system.controller;

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
@RestController
@RequestMapping("/monitor/server")
public class ServerController {
	
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorServerList)
	@GetMapping()
	public R<?> getInfo() throws Exception {
		Server server = new Server();
		server.copyTo();
		return R.ok(server);
	}
}
