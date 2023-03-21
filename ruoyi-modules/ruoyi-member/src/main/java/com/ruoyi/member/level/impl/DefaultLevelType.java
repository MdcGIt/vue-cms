package com.ruoyi.member.level.impl;

import org.springframework.stereotype.Component;

import com.ruoyi.member.level.ILevelType;

@Component(ILevelType.BEAN_PREFIX + DefaultLevelType.ID)
public class DefaultLevelType implements ILevelType {

	public static final String ID = "Default";
	
	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "默认等级类型";
	}
}
