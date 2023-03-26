package com.ruoyi.vote.core.impl;

import org.springframework.stereotype.Component;

import com.ruoyi.vote.core.IVoteItemType;

@Component(IVoteItemType.BEAN_PREFIX + TextVoteItemType.ID)
public class TextVoteItemType implements IVoteItemType {

	public static final String ID = "Text";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "$t{VOTE.ITEM_TYPE.TEXT}";
	}
}
