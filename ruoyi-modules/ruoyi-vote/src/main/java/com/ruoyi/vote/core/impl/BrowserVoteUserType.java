package com.ruoyi.vote.core.impl;

import org.springframework.stereotype.Component;

import com.ruoyi.vote.core.IVoteUserType;

@Component(IVoteUserType.BEAN_PREFIX + BrowserVoteUserType.ID)
public class BrowserVoteUserType implements IVoteUserType {

	public static final String ID = "BrowserFingerprint";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "$t{VOTE.ITEM_TYPE.BROWSER}";
	}
	
}
