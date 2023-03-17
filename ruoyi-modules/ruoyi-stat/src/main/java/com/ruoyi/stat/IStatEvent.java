package com.ruoyi.stat;

public interface IStatEvent {

	/**
	 * 事件类型
	 */	
	public String getEventType();
	
	/**
	 * 事件发生时间戳
	 */
	public Long getTimestamp();
}
