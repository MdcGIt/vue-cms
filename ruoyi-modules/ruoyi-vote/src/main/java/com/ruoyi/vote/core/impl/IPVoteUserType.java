package com.ruoyi.vote.core.impl;

import org.springframework.stereotype.Component;

import com.ruoyi.vote.core.IVoteUserType;

@Component(IVoteUserType.BEAN_PREFIX + IPVoteUserType.ID)
public class IPVoteUserType implements IVoteUserType {

	public static final String ID = "ip";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "$t{VOTE.ITEM_TYPE.IP}";
	}
	
}
