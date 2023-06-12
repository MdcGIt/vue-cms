package com.ruoyi.common.mybatisplus;

import com.ruoyi.common.exception.ErrorCode;

public enum MybatisPlusErrorCode implements ErrorCode {
	
	/**
	 * 不支持的数据库类型：{0}
	 */
	UNSUPPORTED_DB_TYPE;
	
	@Override
	public String value() {
		return "{ERRCODE.DB." + this.name() + "}";
	}
}
