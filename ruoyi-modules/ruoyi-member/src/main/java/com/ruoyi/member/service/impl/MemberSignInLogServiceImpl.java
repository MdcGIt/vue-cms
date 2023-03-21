package com.ruoyi.member.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.member.domain.MemberSignInLog;
import com.ruoyi.member.domain.dto.MemberComplementHistoryDTO;
import com.ruoyi.member.exception.MemberErrorCode;
import com.ruoyi.member.mapper.MemberSignInLogMapper;
import com.ruoyi.member.service.IMemberSignInLogService;

@Service
public class MemberSignInLogServiceImpl extends ServiceImpl<MemberSignInLogMapper, MemberSignInLog>
		implements IMemberSignInLogService {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Override
	public void doSignIn(LoginUser loginUser) {
		LocalDateTime now = LocalDateTime.now();
		Integer signInKey = Integer.valueOf(now.format(FORMATTER));

		Long count = this.lambdaQuery().eq(MemberSignInLog::getMemberId, loginUser.getUserId())
				.eq(MemberSignInLog::getSignInKey, signInKey).count();
		Assert.isTrue(count == 0, MemberErrorCode.SIGN_IN_COMPLETED::exception);
		
		MemberSignInLog signInLog = new MemberSignInLog();
		signInLog.setMemberId(loginUser.getUserId());
		signInLog.setSignInKey(signInKey);
		signInLog.setLogTime(now);
		this.save(signInLog);
	}

	@Override
	public void complementHistory(MemberComplementHistoryDTO dto) {
		LocalDateTime signInDay = LocalDateTime.of(dto.getYear(), dto.getMonth(), dto.getDay(), 0, 0, 0);
		Integer signInKey = Integer.valueOf(signInDay.format(FORMATTER));

		Long count = this.lambdaQuery().eq(MemberSignInLog::getMemberId, dto.getOperator().getUserId())
				.eq(MemberSignInLog::getSignInKey, signInKey).count();
		Assert.isTrue(count == 0, MemberErrorCode.SIGN_IN_COMPLETED::exception);
		
		MemberSignInLog signInLog = new MemberSignInLog();
		signInLog.setMemberId(dto.getOperator().getUserId());
		signInLog.setSignInKey(signInKey);
		signInLog.setLogTime(LocalDateTime.now());
		this.save(signInLog);
	}
}