package com.ruoyi.system.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.async.enums.TaskStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsyncTaskVO {
	
	/**
	 * 任务ID
	 */
	private String taskId;
	
	/**
	 * 任务类型
	 */
	private String type;
	
	/**
	 * 任务状态
	 */
	private TaskStatus status;
	
	/**
	 * 任务准备执行时间，进入线程池队列的时间
	 */
	private LocalDateTime readyTime;
	
	/**
	 * 任务开始时间
	 */
	private LocalDateTime startTime;
	
	/**
	 * 任务结束时间
	 */
	private LocalDateTime endTime;
	
	/**
	 * 任务进度百分比
	 */
	private int percent;
	
	/**
	 * 任务进度描述
	 */
	private String progressMessage;
	
	/**
	 * 任务错误信息记录
	 */
	private List<String> errMessages;
	
	public AsyncTaskVO(AsyncTask task) {
		this.taskId = task.getTaskId();
		this.type = task.getType();
		this.status = task.getStatus();
		this.readyTime = task.getReadyTime();
		this.startTime = task.getStartTime();
		this.endTime = task.getEndTime();
		this.percent = task.getPercent();
		this.progressMessage = task.getProgressMessage();
		this.errMessages = task.getErrMessages();
	}
}

