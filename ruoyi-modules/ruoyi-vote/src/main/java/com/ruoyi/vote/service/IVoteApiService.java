package com.ruoyi.vote.service;

import com.ruoyi.vote.domain.vo.VoteVO;

/**
 * 调查投票前台业务服务类
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface IVoteApiService {

	/**
	 * 获取投票详情
	 * 
	 * @param voteId
	 * @return
	 */
	VoteVO getVote(Long voteId);

	
}
