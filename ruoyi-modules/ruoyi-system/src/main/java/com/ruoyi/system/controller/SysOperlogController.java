package com.ruoyi.system.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysOperLog;
import com.ruoyi.system.permission.SysMenuPriv;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.service.ISysOperLogService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * 操作日志记录
 * 
 * @author ruoyi
 */
@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorLogsView)
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController extends BaseRestController {

	private final ISysOperLogService operLogService;

	@GetMapping("/list")
	public R<?> list(SysOperLog operLog) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysOperLog> q = new LambdaQueryWrapper<SysOperLog>()
				.like(StringUtils.isNotEmpty(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle())
				.like(StringUtils.isNotEmpty(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName())
				.eq(Objects.nonNull(operLog.getOperatorType()), SysOperLog::getOperatorType, operLog.getOperatorType())
				.eq(Objects.nonNull(operLog.getResponseCode()), SysOperLog::getResponseCode, operLog.getResponseCode())
				.ge(Objects.nonNull(operLog.getParams().get("beginTime")), SysOperLog::getOperTime, operLog.getParams().get("beginTime"))
				.le(Objects.nonNull(operLog.getParams().get("endTime")), SysOperLog::getOperTime, operLog.getParams().get("endTime"))
				.orderByDesc(SysOperLog::getOperId);
		Page<SysOperLog> page = operLogService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	@Log(title = "操作日志", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, SysOperLog operLog) {
		LambdaQueryWrapper<SysOperLog> q = new LambdaQueryWrapper<SysOperLog>()
				.like(StringUtils.isNotEmpty(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle())
				.like(StringUtils.isNotEmpty(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName())
				.eq(Objects.nonNull(operLog.getOperatorType()), SysOperLog::getOperatorType, operLog.getOperatorType())
				.eq(Objects.nonNull(operLog.getResponseCode()), SysOperLog::getResponseCode, operLog.getResponseCode())
				.ge(Objects.nonNull(operLog.getParams().get("beginTime")), SysOperLog::getOperTime, operLog.getParams().get("beginTime"))
				.le(Objects.nonNull(operLog.getParams().get("endTime")), SysOperLog::getOperTime, operLog.getParams().get("endTime"))
				.orderByDesc(SysOperLog::getOperId);
		List<SysOperLog> list = operLogService.list(q);
		this.exportExcel(list, SysOperLog.class, response);
	}

	@Log(title = "操作日志", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> operIds) {
		operLogService.removeByIds(operIds);
		return R.ok();
	}

	@Log(title = "操作日志", businessType = BusinessType.CLEAN)
	@DeleteMapping("/clean")
	public R<?> clean() {
		operLogService.cleanOperLog();
		return R.ok();
	}
}
