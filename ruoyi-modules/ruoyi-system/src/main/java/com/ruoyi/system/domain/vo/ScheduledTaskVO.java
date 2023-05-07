package com.ruoyi.system.domain.vo;

import java.time.LocalDateTime;

import com.ruoyi.system.domain.SysScheduledTask;
import com.ruoyi.system.schedule.ScheduledTask;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScheduledTaskVO {
	
	/**
	 * 任务ID
	 */
	private Long taskId;
	
	/**
	 * 任务类型
	 */
	private String taskType;
	
	/**
	 * 任务状态
	 */
	private String status;
	
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
	
	public ScheduledTaskVO(SysScheduledTask task, ScheduledTask scheduledTask) {
		this.taskId = task.getTaskId();
		this.taskType = task.getTaskType();
		this.status = task.getStatus();
		if (scheduledTask != null) {
			this.readyTime = scheduledTask.getReadyTime();
			this.startTime = scheduledTask.getStartTime();
			this.endTime = scheduledTask.getEndTime();
		}
	}
}

