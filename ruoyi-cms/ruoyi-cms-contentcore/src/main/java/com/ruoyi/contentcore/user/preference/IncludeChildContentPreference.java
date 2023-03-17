package com.ruoyi.contentcore.user.preference;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.system.fixed.dict.YesOrNo;
import com.ruoyi.system.user.preference.IUserPreference;

import lombok.RequiredArgsConstructor;

/**
 * 内容列表是否显示子栏目内容
 */
@Component
@RequiredArgsConstructor
public class IncludeChildContentPreference implements IUserPreference {
	
	public static final String ID = "IncludeChildContent";
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "内容列表是否显示子栏目内容";
	}
	
	@Override
	public boolean validate(Object config) {
		return YesOrNo.YES.equals(config) || YesOrNo.NO.equals(config);
	}
	
	@Override
	public Object getDefaultValue() {
		return YesOrNo.NO;
	}

	public static boolean getValue(LoginUser loginUser) {
		SysUser user = (SysUser) loginUser.getUser();
		return YesOrNo.isYes(MapUtils.getString(user.getPreferences(), ID, YesOrNo.NO));
	}
}
