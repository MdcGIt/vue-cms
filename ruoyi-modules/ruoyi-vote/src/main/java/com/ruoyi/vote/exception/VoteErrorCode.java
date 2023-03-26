package com.ruoyi.vote.exception;

import com.ruoyi.common.exception.ErrorCode;

public enum VoteErrorCode implements ErrorCode {
	
	/**
	 * 不支持的投票用户类型：{0}
	 */
	UNSUPPORTED_VOTE_USER_TYPE,

	/**
	 * 不支持的投票选项类型：{0}
	 */
	UNSUPPORTED_VOTE_ITEM_TYPE;
	
	@Override
	public String value() {
		return "ERRCODE.VOTE." + this.name();
	}
}
