package com.ruoyi.member.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.SecurityUtils;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.CDKeyUtil;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.member.domain.Member;
import com.ruoyi.member.domain.MemberLevel;
import com.ruoyi.member.domain.MemberLevelExpLog;
import com.ruoyi.member.domain.MemberSignInLog;
import com.ruoyi.member.domain.dto.MemberDTO;
import com.ruoyi.member.exception.MemberErrorCode;
import com.ruoyi.member.mapper.MemberLevelExpLogMapper;
import com.ruoyi.member.mapper.MemberLevelMapper;
import com.ruoyi.member.mapper.MemberMapper;
import com.ruoyi.member.mapper.MemberSignInLogMapper;
import com.ruoyi.member.service.IMemberService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements IMemberService {

	private final MemberLevelMapper memberLevelMapper;

	private final MemberLevelExpLogMapper memberLevelExpLogMapper;

	private final MemberSignInLogMapper memberSignInLogMapper;

	@Override
	public void addMember(MemberDTO dto) {
		boolean allBlank = StringUtils.isAllBlank(dto.getUserName(), dto.getPhoneNumber(), dto.getEmail());
		Assert.isFalse(allBlank, MemberErrorCode.USERNAME_PHONE_EMAIL_ALL_EMPTY::exception);

		this.checkMemberUnique(dto.getUserName(), dto.getPhoneNumber(), dto.getEmail(), null);

		Member member = new Member();
		member.setMemberId(IdUtils.getSnowflakeId());
		member.setUserName(dto.getUserName());
		if (StringUtils.isEmpty(member.getUserName())) {
			member.setUserName(CDKeyUtil.genChar56(member.getMemberId(), 10));
		}
		member.setEmail(dto.getEmail());
		member.setPhoneNumber(dto.getPhoneNumber());
		member.setNickName(dto.getNickName());
		member.setBirthday(dto.getBirthday());
		member.setPassword(SecurityUtils.passwordEncode(dto.getPassword()));
		member.setStatus(dto.getStatus());
		member.setRemark(dto.getRemark());
		member.createBy(dto.getOperator().getUsername());
		this.save(member);
	}

	@Override
	public void updateMember(MemberDTO dto) {
		Member member = this.getById(dto.getMemberId());
		Assert.notNull(member, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("memberId", dto.getMemberId()));

		this.checkMemberUnique(dto.getUserName(), dto.getPhoneNumber(), dto.getEmail(), dto.getMemberId());

		member.setEmail(dto.getEmail());
		member.setPhoneNumber(dto.getPhoneNumber());
		member.setNickName(dto.getNickName());
		member.setBirthday(dto.getBirthday());
		member.setStatus(dto.getStatus());
		member.setRemark(dto.getRemark());
		member.updateBy(dto.getOperator().getUsername());
		this.updateById(member);
	}

	@Override
	public void deleteMembers(List<Long> memberIds) {
		this.removeByIds(memberIds);
		// 删除会员等级信息
		this.memberLevelMapper.delete(new LambdaQueryWrapper<MemberLevel>().in(MemberLevel::getMemberId, memberIds));
		// 删除会员等级经验日志
		this.memberLevelExpLogMapper
				.delete(new LambdaQueryWrapper<MemberLevelExpLog>().in(MemberLevelExpLog::getMemberId, memberIds));
		// 删除会员签到日志
		this.memberSignInLogMapper
				.delete(new LambdaQueryWrapper<MemberSignInLog>().in(MemberSignInLog::getMemberId, memberIds));
	}

	/**
	 * 校验用户名、手机号、邮箱是否已存在
	 * 
	 * @param member
	 * @return
	 */
	private void checkMemberUnique(String userName, String phoneNumber, String email, Long memberId) {
		Optional<Member> oneOpt = this.lambdaQuery()
				.and(wrapper -> wrapper.eq(StringUtils.isNotEmpty(userName), Member::getUserName, userName).or()
						.eq(StringUtils.isNotEmpty(phoneNumber), Member::getPhoneNumber, phoneNumber).or()
						.eq(StringUtils.isNotEmpty(email), Member::getEmail, email))
				.ne(IdUtils.validate(memberId), Member::getMemberId, memberId).oneOpt();
		if (oneOpt.isPresent()) {
			Member member = oneOpt.get();
			Assert.isFalse(StringUtils.isNotEmpty(userName) && userName.equals(member.getUserName()),
					MemberErrorCode.USERNAME_CONFLICT::exception);
			Assert.isFalse(StringUtils.isNotEmpty(phoneNumber) && phoneNumber.equals(member.getPhoneNumber()),
					MemberErrorCode.PHONE_CONFLICT::exception);
			Assert.isFalse(StringUtils.isNotEmpty(email) && email.equals(member.getEmail()),
					MemberErrorCode.EMAIL_CONFLICT::exception);
		}
	}
}