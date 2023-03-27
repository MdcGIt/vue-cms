package com.ruoyi.vote.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.vote.domain.VoteSubject;
import com.ruoyi.vote.domain.dto.SaveSubjectItemsDTO;

import jakarta.validation.constraints.NotEmpty;

public interface IVoteSubjectService extends IService<VoteSubject> {

	/**
	 * 获取指定调查投票的所有主题列表
	 * 
	 * @param voteId
	 * @return
	 */
	List<VoteSubject> getVoteSubjectList(Long voteId);

	/**
	 * 添加投票主题数据
	 * 
	 * @param voteSubject
	 */
	void addVoteSubject(VoteSubject voteSubject);

	/**
	 * 更新投票主题数据
	 * 
	 * @param voteSubject
	 */
	void updateVoteSubject(VoteSubject voteSubject);

	/**
	 * 删除投票主题数据
	 * 
	 * @param subjectIds
	 */
	void deleteVoteSubjects(@NotEmpty List<Long> subjectIds);

	/**
	 * 保存主题选项列表
	 * 
	 * @param dto
	 */
	void saveSubjectItems(SaveSubjectItemsDTO dto);

}