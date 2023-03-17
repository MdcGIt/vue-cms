package com.ruoyi.system.mapper;

import org.apache.ibatis.annotations.Delete;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.system.domain.SysOperLog;

/**
 * 操作日志 数据层
 */
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

	/**
	 * 清空操作日志
	 */
	@Delete("truncate table " + SysOperLog.TABLE_NAME)
	public void cleanOperLog();
}
