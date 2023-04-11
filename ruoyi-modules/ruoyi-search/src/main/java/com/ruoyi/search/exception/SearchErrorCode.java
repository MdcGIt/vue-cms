package com.ruoyi.search.exception;

import com.ruoyi.common.exception.ErrorCode;

public enum SearchErrorCode implements ErrorCode {
	
	/**
	 * 字典词“{0}”已存在
	 */
	DICT_WORD_EXISTS,
	
	/**
	 * 索引模型“{0}”不存在
	 */
	MODEL_NOT_EXISTS,
	
	/**
	 * 不支持的检索类型：{0}
	 */
	UNSUPPORTED_SEARCH_TYPE;
	
	@Override
	public String value() {
		return "{ERRCODE.SEARCH." + this.name() + "}";
	}
}
