package com.ruoyi.stat;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 统计事件处理器
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface IStatEventHandler {
	
	/**
	 * 处理器唯一标识
	 * 
	 * @param eventType
	 * @return
	 */
	public String getId();

	/**
	 * 处理统计事件
	 * 
	 * @param event
	 */
	public void handle(HttpServletRequest request);
}
