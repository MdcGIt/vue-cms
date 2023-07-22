package com.ruoyi.cms.member.exception;

import com.ruoyi.common.exception.ErrorCode;

public enum CmsMemberErrorCode implements ErrorCode {
	
	/**
	 * 已收藏过的内容
	 */
	FAVORITED,

	/**
	 * 已点赞过的内容
	 */
	LIKED;

	@Override
	public String value() {
		return "{ERRCODE.CMS.MEMBER." + this.name() + "}";
	}
}
