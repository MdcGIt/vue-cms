package com.ruoyi.system.logs.impl;

import org.springframework.stereotype.Component;

import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.system.logs.ILogMenu;

@Component
public class UserOperationLogMenu implements ILogMenu {

	@Override
	public String getId() {
		return "UserOperation";
	}

	@Override
	public String getName() {
		return I18nUtils.get("LOG.MENU." + getId());
	}

	@Override
	public String getRouter() {
		return "/monitor/logs/operation";
	}
}
