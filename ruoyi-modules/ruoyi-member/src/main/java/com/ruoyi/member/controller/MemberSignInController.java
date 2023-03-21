package com.ruoyi.member.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.member.domain.MemberSignInLog;
import com.ruoyi.member.domain.dto.MemberComplementHistoryDTO;
import com.ruoyi.member.security.SaMemberCheckLogin;
import com.ruoyi.member.security.StpMemberUtil;
import com.ruoyi.member.service.IMemberSignInLogService;
import com.ruoyi.system.security.StpAdminUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/member/signIn")
public class MemberSignInController extends BaseRestController {

	private final IMemberSignInLogService memberSignInLogService;

	/**
	 * 获取会员指定月份签到数据，默认：当前月份
	 */
	@SaMemberCheckLogin
	@GetMapping
	public R<?> getMonthSignInLog(@RequestParam(value = "year", required = false) Integer year,
			@RequestParam(value = "month", required = false) Integer month) {
		LocalDateTime now = LocalDateTime.now();
		if (Objects.isNull(year)) {
			year = now.getYear();
		}
		if (Objects.isNull(month)) {
			month = now.getMonthValue();
		}
		LocalDateTime startTime = LocalDateTime.of(year, month, 1, 0, 0, 0);

		int endYear = year;
		int endMonth = month;
		if (month == 12) {
			endYear = year + 1;
			endMonth = 1;
		}
		LocalDateTime endTime = LocalDateTime.of(endYear, endMonth, 1, 0, 0, 0);
		List<MemberSignInLog> list = this.memberSignInLogService.lambdaQuery()
				.eq(MemberSignInLog::getMemberId, StpMemberUtil.getLoginId()).ge(MemberSignInLog::getLogTime, startTime)
				.lt(MemberSignInLog::getLogTime, endTime).list();
		return this.bindDataTable(list);
	}

	/**
	 * 签到
	 */
	@SaMemberCheckLogin
	@PostMapping
	public R<?> signIn() {
		this.memberSignInLogService.doSignIn(StpMemberUtil.getLoginUser());
		return R.ok();
	}

	/**
	 * 补签
	 */
	@SaMemberCheckLogin
	@PutMapping
	public R<?> complementHistory(@RequestBody MemberComplementHistoryDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.memberSignInLogService.complementHistory(dto);
		return R.ok();
	}
}