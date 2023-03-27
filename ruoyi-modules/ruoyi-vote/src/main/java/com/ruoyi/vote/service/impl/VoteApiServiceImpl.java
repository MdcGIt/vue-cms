package com.ruoyi.vote.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.vote.domain.Vote;
import com.ruoyi.vote.domain.VoteSubject;
import com.ruoyi.vote.domain.VoteSubjectItem;
import com.ruoyi.vote.domain.vo.VoteSubjectVO;
import com.ruoyi.vote.domain.vo.VoteVO;
import com.ruoyi.vote.mapper.VoteMapper;
import com.ruoyi.vote.mapper.VoteSubjectItemMapper;
import com.ruoyi.vote.mapper.VoteSubjectMapper;
import com.ruoyi.vote.service.IVoteApiService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteApiServiceImpl implements IVoteApiService {

	private static final String CACHE_PREFIX = "voteapi:";

	private final VoteMapper voteMapper;

	private final VoteSubjectMapper subjectMapper;

	private final VoteSubjectItemMapper subjectItemMapper;

	private final RedisCache redisCache;

	/**
	 * 获取投票详情
	 * 
	 * @param voteId
	 * @return
	 */
	@Override
	public VoteVO getVote(Long voteId) {
		return this.redisCache.getCacheObject(CACHE_PREFIX + voteId, () -> loadVoteDetail(voteId));
	}

	private VoteVO loadVoteDetail(Long voteId) {
		Vote vote = this.voteMapper.selectById(voteId);
		Assert.notNull(vote, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("voteId", voteId));

		VoteVO voteVO = new VoteVO();
		voteVO.setVoteId(vote.getVoteId());
		voteVO.setTitle(vote.getTitle());
		voteVO.setStartTime(vote.getStartTime());
		voteVO.setEndTime(vote.getEndTime());
		voteVO.setDayLimit(vote.getDayLimit());
		voteVO.setTotalLimit(vote.getTotalLimit());
		voteVO.setViewType(vote.getViewType());
		voteVO.setTotal(voteVO.getTotal());

		new LambdaQueryChainWrapper<>(this.subjectItemMapper).eq(VoteSubjectItem::getVoteId, vote.getVoteId());

		List<VoteSubjectVO> subjects = new LambdaQueryChainWrapper<>(this.subjectMapper)
				.eq(VoteSubject::getVoteId, vote.getVoteId()).list().stream().map(subject -> {
					VoteSubjectVO voteSubjectVO = new VoteSubjectVO();
					voteSubjectVO.setSubjectId(subject.getSubjectId());
					voteSubjectVO.setTitle(subject.getTitle());
					voteSubjectVO.setType(subject.getType());
					voteSubjectVO.setSortFlag(subject.getSortFlag());
					voteSubjectVO.setItems(null);
					return voteSubjectVO;
				}).toList();
		voteVO.setSubjects(subjects);
		return voteVO;
	}
}
