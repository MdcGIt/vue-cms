package com.ruoyi.system.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.extend.annotation.XssIgnore;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.domain.dto.SysNoticeDTO;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysNoticeService;

import lombok.RequiredArgsConstructor;

/**
 * 公告 信息操作处理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseRestController {

	private final ISysNoticeService noticeService;

	/**
	 * 获取通知公告列表
	 */
	@Priv(type = AdminUserType.TYPE, value = "system:notice:list")
	@GetMapping("/list")
	public R<?> list(SysNotice notice) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysNotice> q = new LambdaQueryWrapper<SysNotice>()
				.like(StringUtils.isNotEmpty(notice.getNoticeTitle()), SysNotice::getNoticeTitle, notice.getNoticeTitle())
				.like(StringUtils.isNotEmpty(notice.getNoticeType()), SysNotice::getNoticeType, notice.getNoticeType())
				.eq(StringUtils.isNotEmpty(notice.getCreateBy()), SysNotice::getCreateBy, notice.getCreateBy())
				.orderByDesc(SysNotice::getNoticeId);
		Page<SysNotice> page = noticeService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	/**
	 * 根据通知公告编号获取详细信息
	 */
	@Priv(type = AdminUserType.TYPE, value = "system:notice:query")
	@GetMapping(value = "/{noticeId}")
	public R<?> getInfo(@PathVariable Long noticeId) {
		Assert.isTrue(IdUtils.validate(noticeId), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception("noticeId: " + noticeId));
		return R.ok(noticeService.getById(noticeId));
	}

	/**
	 * 新增通知公告
	 */
	@XssIgnore
	@Priv(type = AdminUserType.TYPE, value = "system:notice:add")
	@Log(title = "通知公告", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysNoticeDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		noticeService.insertNotice(dto);
		return R.ok();
	}

	/**
	 * 修改通知公告
	 */
	@XssIgnore
	@Priv(type = AdminUserType.TYPE, value = "system:notice:edit")
	@Log(title = "通知公告", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysNoticeDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		noticeService.updateNotice(dto);
		return R.ok();
	}

	/**
	 * 删除通知公告
	 */
	@Priv(type = AdminUserType.TYPE, value = "system:notice:remove")
	@Log(title = "通知公告", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody List<Long> noticeIds) {
		noticeService.deleteNoticeByIds(noticeIds);
		return R.ok();
	}
}
