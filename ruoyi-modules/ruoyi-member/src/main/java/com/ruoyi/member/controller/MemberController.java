package com.ruoyi.member.controller;

import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.member.domain.Member;
import com.ruoyi.member.domain.dto.MemberDTO;
import com.ruoyi.member.domain.vo.MemberListVO;
import com.ruoyi.member.fixed.config.EncryptMemberPhoneAndEmail;
import com.ruoyi.member.permission.MemberPriv;
import com.ruoyi.member.service.IMemberService;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@Priv(type = AdminUserType.TYPE, value = MemberPriv.MemberList)
@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController extends BaseRestController {

	private final IMemberService memberService;

	@GetMapping
	public R<?> getPageList(@RequestParam(required = false) String userName,
			@RequestParam(required = false) String nickName, @RequestParam(required = false) String email,
			@RequestParam(required = false) String phonenumber, @RequestParam(required = false) String status) {
		PageRequest pr = this.getPageRequest();
		Page<Member> page = this.memberService.lambdaQuery()
				.like(StringUtils.isNotEmpty(userName), Member::getUserName, userName)
				.like(StringUtils.isNotEmpty(nickName), Member::getNickName, nickName)
				.like(StringUtils.isNotEmpty(email), Member::getEmail, email)
				.like(StringUtils.isNotEmpty(phonenumber), Member::getPhonenumber, phonenumber)
				.eq(StringUtils.isNotEmpty(status), Member::getStatus, status)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		List<MemberListVO> list = page.getRecords().stream().map(m -> {
			MemberListVO vo = new MemberListVO();
			BeanUtils.copyProperties(m, vo);
			if (EncryptMemberPhoneAndEmail.getValue()) {
				vo.setEmail(EncryptMemberPhoneAndEmail.encryptEmail(vo.getEmail()));
				vo.setPhoneNumber(EncryptMemberPhoneAndEmail.encryptPhone(vo.getPhoneNumber()));
			}
			return vo;
		}).toList();
		return this.bindDataTable(list, page.getTotal());
	}

	@GetMapping("/{memberId}")
	public R<?> getExpOperationDetail(@PathVariable @Min(1) Long memberId) {
		Member member = this.memberService.getById(memberId);
		Assert.notNull(member, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("memberId", memberId));
		MemberListVO vo = new MemberListVO();
		BeanUtils.copyProperties(member, vo);
		return R.ok(vo);
	}

	@Log(title = "新增会员", businessType = BusinessType.INSERT, isSaveRequestData = false)
	@PostMapping
	public R<?> addMember(@RequestBody MemberDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.memberService.addMember(dto);
		return R.ok();
	}

	@Log(title = "编辑会员", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> updateMember(@RequestBody MemberDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.memberService.updateMember(dto);
		return R.ok();
	}

	@Log(title = "删除会员", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> delete(@RequestBody @NotEmpty List<Long> memberIds) {
		IdUtils.validate(memberIds);
		this.memberService.deleteMembers(memberIds);
		return R.ok();
	}

	@Log(title = "重置会员密码", businessType = BusinessType.UPDATE, isSaveRequestData = false)
	@PutMapping("/resetPassword")
	public R<?> resetPassword(@RequestBody MemberDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.memberService.resetPwd(dto);
		return R.ok();
	}
}