package com.ruoyi.system.service.impl;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.ruoyi.system.schedule.IScheduledHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.properties.XxlJobProperties;
import com.ruoyi.system.domain.SysScheduledTask;
import com.ruoyi.system.domain.SysScheduledTaskLog;
import com.ruoyi.system.domain.dto.ScheduledTaskDTO;
import com.ruoyi.system.exception.SysErrorCode;
import com.ruoyi.system.fixed.dict.EnableOrDisable;
import com.ruoyi.system.fixed.dict.SuccessOrFail;
import com.ruoyi.system.fixed.dict.YesOrNo;
import com.ruoyi.system.mapper.SysScheduledTaskLogMapper;
import com.ruoyi.system.mapper.SysScheduledTaskMapper;
import com.ruoyi.system.schedule.ScheduledTask;
import com.ruoyi.system.schedule.ScheduledTaskStatus;
import com.ruoyi.system.schedule.ScheduledTaskTriggerType;
import com.ruoyi.system.schedule.ScheduledTaskTriggerType.CronTriggerArgs;
import com.ruoyi.system.schedule.ScheduledTaskTriggerType.PeriodicTriggerArgs;
import com.ruoyi.system.service.ISysScheduledTaskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class SysScheduledTaskServiceImpl extends ServiceImpl<SysScheduledTaskMapper, SysScheduledTask>
        implements ISysScheduledTaskService, CommandLineRunner {

    private static final ConcurrentHashMap<Long, ScheduledTask> taskMap = new ConcurrentHashMap<>();

    private final XxlJobProperties xxlJobProperties;

    private final SysScheduledTaskLogMapper taskLogMapper;

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private final Map<String, IScheduledHandler> taskHandlers;

    public static Trigger createTrigger(String triggerType, String triggerArgJson) {
        if (ScheduledTaskTriggerType.isCron(triggerType)) {
            CronTriggerArgs args = JacksonUtils.from(triggerArgJson, CronTriggerArgs.class);
            return new CronTrigger(args.getCron());
        } else if (ScheduledTaskTriggerType.isPeriodic(triggerType)) {
            PeriodicTriggerArgs args = JacksonUtils.from(triggerArgJson, PeriodicTriggerArgs.class);
            Assert.isTrue(args.getSeconds() > 0, () -> SysErrorCode.SCHEDULED_TASK_TRIGGER_ERR
                    .exception(triggerType, triggerArgJson));

            PeriodicTrigger periodicTrigger = new PeriodicTrigger(Duration.ofSeconds(args.getSeconds()));
            periodicTrigger.setFixedRate(args.getFixedRate());
            if (args.getDelaySeconds() > 0) {
                periodicTrigger.setInitialDelay(Duration.ofSeconds(args.getDelaySeconds()));
            }
            return periodicTrigger;
        }
        throw SysErrorCode.SCHEDULED_TASK_TRIGGER_ERR.exception(triggerType, triggerArgJson);
    }

    @Override
    public void run(String... args) throws Exception {
        this.lambdaQuery().eq(SysScheduledTask::getStatus, EnableOrDisable.ENABLE).list()
                .forEach(this::addScheduledTask);
    }

    private IScheduledHandler getScheduledHandler(String taskType) {
        IScheduledHandler handler = this.taskHandlers.get(IScheduledHandler.BEAN_PREFIX + taskType);
        Assert.notNull(handler, () -> SysErrorCode.SCHEDULED_TASK_UNSUPPORTED_HANDLER.exception(taskType));
        return handler;
    }

    private void addScheduledTask(SysScheduledTask task) {
        IScheduledHandler handler = this.getScheduledHandler(task.getTaskType());
        Trigger trigger = createTrigger(task.getTaskTrigger(), task.getTriggerArgs());
        ScheduledTask scheduledTask = new ScheduledTask(this) {

            @Override
            public void run0() throws Exception {
                handler.exec();
            }
        };
        scheduledTask.setTaskId(task.getTaskId());
        scheduledTask.setType(task.getTaskType());
        this.taskMap.put(task.getTaskId(), scheduledTask);

        scheduledTask.ready();
        ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(scheduledTask, trigger);
        scheduledTask.setFuture(future);
    }

    @Override
    public void addTaskLog(ScheduledTask task) {
        SysScheduledTaskLog taskLog = new SysScheduledTaskLog();
        taskLog.setTaskId(task.getTaskId());
        taskLog.setTaskType(task.getType());
        taskLog.setResult(task.getStatus() == ScheduledTaskStatus.SUCCESS ? SuccessOrFail.SUCCESS : SuccessOrFail.FAIL);
        taskLog.setReadyTime(task.getReadyTime());
        taskLog.setStartTime(task.getStartTime());
        taskLog.setEndTime(task.getEndTime());
        taskLog.setInterruptTime(task.getInterruptTime());
        taskLog.setLogTime(LocalDateTime.now());
        taskLog.setPercent(task.getPercent());
        if (StringUtils.isNotEmpty(task.getErrMessages())) {
            StringJoiner stringJoiner = new StringJoiner(StringUtils.COMMA);
            for (String err : task.getErrMessages()) {
                if (stringJoiner.length() + err.length() >= 2000) {
                    break; // 只保存前2000个字符的错误信息
                }
                stringJoiner.add(err);
            }
            taskLog.setMessage(stringJoiner.toString());
        }
        this.taskLogMapper.insert(taskLog);
    }

    @Override
    public void insertTask(ScheduledTaskDTO dto) {
        long count = this.lambdaQuery().eq(SysScheduledTask::getTaskType, dto.getTaskType()).count();
        Assert.isTrue(count == 0, () -> SysErrorCode.SCHEDULED_TASK_EXISTS.exception());

        SysScheduledTask task = new SysScheduledTask();
        task.setTaskId(IdUtils.getSnowflakeId());
        task.setTaskType(dto.getTaskType());
        task.setStatus(dto.getStatus());
        task.setTaskTrigger(dto.getTaskTrigger());
        if (ScheduledTaskTriggerType.isCron(task.getTaskTrigger())) {
            CronTriggerArgs args = new CronTriggerArgs();
            args.setCron(dto.getCron());
            task.setTriggerArgs(JacksonUtils.to(args));
        } else {
            PeriodicTriggerArgs args = new PeriodicTriggerArgs();
            args.setFixedRate(YesOrNo.isYes(dto.getFixedRate()));
            args.setSeconds(dto.getSeconds());
            args.setDelaySeconds(dto.getDelaySeconds());
            task.setTriggerArgs(JacksonUtils.to(args));
        }
        task.setRemark(dto.getRemark());
        task.createBy(dto.getOperator().getUsername());

        if (EnableOrDisable.isEnable(task.getStatus())) {
            this.addScheduledTask(task);
        }
        this.save(task);
    }

    @Override
    public void updateTask(ScheduledTaskDTO dto) {
        SysScheduledTask task = this.getById(dto.getTaskId());
        Assert.notNull(task, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("taskId", task.getTaskId()));
        Assert.isTrue(EnableOrDisable.isDisable(task.getStatus()), SysErrorCode.SCHEDULED_TASK_UPDATE_ERR::exception);

        task.setTaskTrigger(dto.getTaskTrigger());
        if (ScheduledTaskTriggerType.isCron(task.getTaskTrigger())) {
            CronTriggerArgs args = new CronTriggerArgs();
            args.setCron(dto.getCron());
            task.setTriggerArgs(JacksonUtils.to(args));
        } else {
            PeriodicTriggerArgs args = new PeriodicTriggerArgs();
            args.setFixedRate(YesOrNo.isYes(dto.getFixedRate()));
            args.setSeconds(dto.getSeconds());
            args.setDelaySeconds(dto.getDelaySeconds());
            task.setTriggerArgs(JacksonUtils.to(args));
        }
        task.setRemark(dto.getRemark());
        task.updateBy(dto.getOperator().getUsername());
        this.updateById(task);
    }

    @Override
    public void deleteTasks(List<Long> taskIds) {
        List<SysScheduledTask> list = this.listByIds(taskIds);
        for (SysScheduledTask task : list) {
            Assert.isTrue(EnableOrDisable.isDisable(task.getStatus()),
                    SysErrorCode.SCHEDULED_TASK_REMOVE_ERR::exception);
        }
        this.removeByIds(list);
        list.forEach(task -> this.removeScheduledTask(task.getTaskId()));
    }

    @Override
    public void enableTask(Long taskId) {
        SysScheduledTask dbTask = this.getById(taskId);
        Assert.notNull(dbTask, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("taskId", taskId));

        if (EnableOrDisable.isEnable(dbTask.getStatus())) {
            return;
        }
        ScheduledTask scheduledTask = this.taskMap.get(taskId);
        Assert.isTrue(Objects.isNull(scheduledTask), SysErrorCode.SCHEDULED_TASK_RUNNING::exception);
        this.addScheduledTask(dbTask);

        dbTask.setStatus(EnableOrDisable.ENABLE);
        this.updateById(dbTask);
    }

    @Override
    public void disableTask(Long taskId) {
        SysScheduledTask dbTask = this.getById(taskId);
        Assert.notNull(dbTask, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("taskId", taskId));

        if (EnableOrDisable.isDisable(dbTask.getStatus())) {
            return;
        }
        dbTask.setStatus(EnableOrDisable.DISABLE);
        this.updateById(dbTask);
        this.removeScheduledTask(dbTask.getTaskId());
    }

    private void removeScheduledTask(Long taskId) {
        ScheduledTask scheduledTask = this.taskMap.get(taskId);
        if (scheduledTask != null) {
            scheduledTask.interrupt();
            scheduledTask.getFuture().cancel(false);
            this.taskMap.remove(taskId);
        }
    }

    @Override
    public void execOnceImmediately(Long taskId) {
        SysScheduledTask task = this.getById(taskId);
        Assert.notNull(task, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("taskId", taskId));
        Assert.isTrue(EnableOrDisable.isDisable(task.getStatus()), SysErrorCode.SCHEDULED_TASK_EXEC_ERR::exception);

        IScheduledHandler handler = this.getScheduledHandler(task.getTaskType());
        ScheduledTask scheduledTask = new ScheduledTask(this) {

            @Override
            public void run0() throws Exception {
                handler.exec();
            }
        };
        scheduledTask.setTaskId(task.getTaskId());
        scheduledTask.setType(task.getTaskType());
        scheduledTask.ready();
        scheduledTask.setEndEvent(t -> this.taskMap.remove(task.getTaskId()));

        this.taskMap.put(task.getTaskId(), scheduledTask);
        threadPoolTaskScheduler.schedule(scheduledTask, Instant.now());
    }

    @Override
    public ScheduledTask getScheduledTask(Long taskId) {
        return this.taskMap.get(taskId);
    }
}
