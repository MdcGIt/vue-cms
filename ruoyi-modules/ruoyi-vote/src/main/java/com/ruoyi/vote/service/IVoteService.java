package com.ruoyi.vote.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.vote.domain.Vote;

public interface IVoteService extends IService<Vote> {

	/**
	 * 添加调查投票
	 * 
	 * @param vote
	 * @return
	 */
	void addVote(Vote vote);
	
	/**
	 * 修改调查投票
	 * 
	 * @param vote
	 */
	void updateVote(Vote vote);
	
	/**
	 * 删除调查投票
	 * 
	 * @param voteIds
	 */
	void deleteVotes(List<Long> voteIds);
}