package com.ruoyi.system.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.system.logs.ILogMenu;
import com.ruoyi.system.security.SaAdminCheckLogin;

import lombok.RequiredArgsConstructor;

/**
 * 日志基础控制器
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/logs")
public class LogController extends BaseRestController {

	private final List<ILogMenu> logMenus;

	@SaAdminCheckLogin
	@GetMapping
	public R<?> getMenus() throws Exception {
		return this.bindDataTable(this.logMenus);
	}
}
