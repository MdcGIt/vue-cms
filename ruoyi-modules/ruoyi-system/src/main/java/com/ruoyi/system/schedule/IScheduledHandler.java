package com.ruoyi.system.schedule;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IScheduledHandler {

    String BEAN_PREFIX = "ScheduledHandler_";

    /**
     * 定时任务ID唯一标识
     *
     * @return
     */
    String getId();

    /**
     * 定时任务名称
     *
     * @return
     */
    String getName();

    /**
     * 执行任务
     */
    void exec() throws Exception;
}
