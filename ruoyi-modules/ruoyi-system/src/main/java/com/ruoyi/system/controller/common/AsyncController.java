package com.ruoyi.system.controller.common;

import java.time.ZoneOffset;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.vo.AsyncTaskVO;
import com.ruoyi.system.exception.SysErrorCode;
import com.ruoyi.system.permission.SysMenuPriv;
import com.ruoyi.system.security.AdminUserType;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/async")
public class AsyncController extends BaseRestController {

	private final AsyncTaskManager asyncTaskManager;

	/**
	 * 异步任务列表
	 * 
	 * @param type 任务类型
	 * @return
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@GetMapping("/task")
	public R<?> getAsyncTaskList(@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "id", required = false) String taskId) {
		List<AsyncTask> taskList = this.asyncTaskManager.getTaskList(type);
		List<AsyncTaskVO> list = taskList.stream().filter(t -> {
			return (StringUtils.isBlank(type) || t.getType().indexOf(type) > -1)
					&& (StringUtils.isBlank(taskId) || t.getTaskId().indexOf(taskId) > -1);
		}).map(AsyncTaskVO::new).sorted((v1, v2) -> v1.getReadyTime().toEpochSecond(ZoneOffset.UTC)
				- v2.getReadyTime().toEpochSecond(ZoneOffset.UTC) > 0 ? 1 : -1).toList();
		return this.bindDataTable(list);
	}

	/**
	 * 异步任务详情
	 * 
	 * @param taskId 任务ID
	 * @return
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@GetMapping("/task/{taskId}")
	public R<?> getAsyncTaskInfo(@PathVariable("taskId") String taskId) {
		AsyncTask task = this.asyncTaskManager.getTask(taskId);
		Assert.notNull(task, () -> SysErrorCode.ASYNC_TASK_NOT_FOUND.exception(taskId));

		return R.ok(new AsyncTaskVO(task));
	}

	/**
	 * 停止异步任务
	 * 
	 * @param taskIds
	 * @return
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@PutMapping("/task/stop")
	public R<?> stopAsyncTask(@RequestBody @NotEmpty List<String> taskIds) {
		for (String taskId : taskIds) {
			this.asyncTaskManager.cancel(taskId);
		}
		return R.ok();
	}

	/**
	 * 删除异步任务
	 * 
	 * @param taskIds
	 * @return
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.AsyncTaskList)
	@DeleteMapping("task/remove")
	public R<?> deleteAsyncTask(@RequestBody @NotEmpty List<String> taskIds) {
		for (String taskId : taskIds) {
			this.asyncTaskManager.removeById(taskId);
		}
		return R.ok();
	}
}
