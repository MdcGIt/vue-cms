package com.ruoyi.vote.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.vote.domain.Vote;
import com.ruoyi.vote.mapper.VoteMapper;
import com.ruoyi.vote.service.IVoteService;

@Service
public class VoteServiceImpl extends ServiceImpl<VoteMapper, Vote> implements IVoteService {

	@Override
	public void addVote(Vote vote) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateVote(Vote vote) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteVotes(List<Long> voteIds) {
		// TODO Auto-generated method stub

	}
}