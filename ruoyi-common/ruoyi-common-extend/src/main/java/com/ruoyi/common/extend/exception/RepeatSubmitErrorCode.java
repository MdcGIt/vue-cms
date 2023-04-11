package com.ruoyi.common.extend.exception;

import com.ruoyi.common.exception.ErrorCode;

public enum RepeatSubmitErrorCode implements ErrorCode {
	
	/**
	 * Lang: 不允许重复提交，请稍候再试
	 */
	REPEAT_ERR;
	
	private RepeatSubmitErrorCode() {
	}
	
	@Override
	public String value() {
		return "{ERRCODE.EXTEND." + this.name() + "}";
	}
}
