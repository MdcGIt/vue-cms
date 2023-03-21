package com.ruoyi.member.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.member.domain.Member;
import com.ruoyi.member.domain.dto.MemberDTO;

public interface IMemberService extends IService<Member> {

	/**
	 * 添加会员信息
	 * 
	 * @param dto
	 * @return
	 */
	void addMember(MemberDTO dto);

	/**
	 * 修改会员信息
	 * 
	 * @param dto
	 * @return
	 */
	void updateMember(MemberDTO dto);
	
	/**
	 * 删除会员信息
	 * 
	 * @param memberIds
	 */
	void deleteMembers(List<Long> memberIds);

	/**
	 * 重置用户密码
	 * 
	 * @param user 用户信息
	 * @return 结果
	 */
	void resetPwd(MemberDTO dto);
}