package com.ruoyi.vote.exception;

import com.ruoyi.common.exception.ErrorCode;

public enum VoteErrorCode implements ErrorCode {
	
	/**
	 * 不支持的问卷调查用户类型：{0}
	 */
	UNSUPPORTED_VOTE_USER_TYPE,

	/**
	 * 不支持的问卷调查选项类型：{0}
	 */
	UNSUPPORTED_VOTE_ITEM_TYPE,
	
	/**
	 * 超出问卷调查参与次数上限
	 */
	VOTE_TOTAL_LIMIT,
	
	/**
	 * 超出问卷调查每日参与次数上限
	 */
	VOTE_DAY_LIMIT;
	
	@Override
	public String value() {
		return "ERRCODE.VOTE." + this.name();
	}
}
