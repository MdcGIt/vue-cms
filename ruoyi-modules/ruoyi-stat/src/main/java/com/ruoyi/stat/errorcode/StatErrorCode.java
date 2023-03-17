package com.ruoyi.stat.errorcode;

import com.ruoyi.common.exception.ErrorCode;

public enum StatErrorCode implements ErrorCode {
	
	/**
	 * Lang: 不支持的统计类型：{0}
	 */
	UNSUPPORT_STAT_TYPE;

	@Override
	public String value() {
		return "ERRCODE.STAT." + this.name();
	}
}
