package com.ruoyi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.system.domain.SysOperLog;

/**
 * 操作日志 服务层
 * 
 * @author ruoyi
 */
public interface ISysOperLogService extends IService<SysOperLog> {
	
	/**
	 * 清空操作日志
	 */
	public void cleanOperLog();

	/**
	 * 操作日志记录
	 * 
	 * @param operLog
	 *            操作日志信息
	 * @return 任务task
	 */
	public AsyncTask recordOper(SysOperLog operLog);
}
