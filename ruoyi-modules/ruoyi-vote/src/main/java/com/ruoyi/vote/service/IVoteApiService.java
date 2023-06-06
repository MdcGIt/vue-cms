package com.ruoyi.vote.service;

import com.ruoyi.vote.domain.dto.VoteSubmitDTO;
import com.ruoyi.vote.domain.vo.VoteVO;

/**
 * 问卷调查前台业务服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IVoteApiService {

	/**
	 * 获取问卷调查详情
	 * 
	 * @param voteId
	 * @return
	 */
	VoteVO getVote(Long voteId);

	/**
	 * 提交问卷调查
	 * 
	 * @param dto
	 */
	void submitVote(VoteSubmitDTO dto);
}
