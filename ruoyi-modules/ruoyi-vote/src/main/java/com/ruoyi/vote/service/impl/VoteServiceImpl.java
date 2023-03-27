package com.ruoyi.vote.service.impl;

import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.vote.domain.Vote;
import com.ruoyi.vote.domain.VoteLog;
import com.ruoyi.vote.domain.VoteSubject;
import com.ruoyi.vote.domain.VoteSubjectItem;
import com.ruoyi.vote.listener.BeforeVoteAddEvent;
import com.ruoyi.vote.mapper.VoteMapper;
import com.ruoyi.vote.service.IVoteLogService;
import com.ruoyi.vote.service.IVoteService;
import com.ruoyi.vote.service.IVoteSubjectItemService;
import com.ruoyi.vote.service.IVoteSubjectService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class VoteServiceImpl extends ServiceImpl<VoteMapper, Vote> implements IVoteService, ApplicationContextAware {
	
	private ApplicationContext applicationContext;

	private final IVoteSubjectService voteSubjectService;

	private final IVoteSubjectItemService voteSubjectItemService;

	private final IVoteLogService voteLogService;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void addVote(Vote vote) {
		this.checkUnique(vote);

		vote.setVoteId(IdUtils.getSnowflakeId());
		vote.setTotal(0);

		this.applicationContext.publishEvent(new BeforeVoteAddEvent(this, vote));
		this.save(vote);
	}

	@Override
	public void updateVote(Vote vote) {
		Vote db = this.getById(vote.getVoteId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("voteId", vote.getVoteId()));
		
		this.checkUnique(db);

		db.setTitle(vote.getTitle());
		db.setCode(vote.getCode());
		db.setUserType(null);
		db.setDayLimit(vote.getDayLimit());
		db.setTotalLimit(vote.getTotalLimit());
		db.setViewType(vote.getViewType());
		db.setStartTime(vote.getStartTime());
		db.setEndTime(vote.getEndTime());
		db.setStatus(vote.getStatus());
		db.setRemark(vote.getRemark());
		this.updateById(db);
		
	}

	private void checkUnique(Vote vote) {
		boolean unique = this.lambdaQuery().eq(Vote::getCode, vote.getCode())
				.ne(IdUtils.validate(vote.getVoteId()), Vote::getVoteId, vote.getVoteId()).count() == 0;
		Assert.isTrue(unique, () -> CommonErrorCode.DATA_CONFLICT.exception("code"));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteVotes(List<Long> voteIds) {
		this.removeByIds(voteIds);
		// 删除投票主题
		this.voteSubjectService.remove(this.voteSubjectService.lambdaQuery().in(VoteSubject::getVoteId, voteIds));
		// 删除投票主题选择项
		this.voteSubjectItemService
				.remove(this.voteSubjectItemService.lambdaQuery().in(VoteSubjectItem::getVoteId, voteIds));
		// 删除投票相关日志
		this.voteLogService.remove(this.voteLogService.lambdaQuery().in(VoteLog::getVoteId, voteIds));
	}
}