package com.ruoyi.vote.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.vote.domain.Vote;
import com.ruoyi.vote.domain.VoteSubject;
import com.ruoyi.vote.domain.VoteSubjectItem;
import com.ruoyi.vote.domain.dto.SaveSubjectItemsDTO;
import com.ruoyi.vote.fixed.dict.VoteSubjectType;
import com.ruoyi.vote.mapper.VoteMapper;
import com.ruoyi.vote.mapper.VoteSubjectItemMapper;
import com.ruoyi.vote.mapper.VoteSubjectMapper;
import com.ruoyi.vote.service.IVoteSubjectService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteSubjectServiceImpl extends ServiceImpl<VoteSubjectMapper, VoteSubject> implements IVoteSubjectService {

	private final VoteMapper voteMapper;

	private final VoteSubjectItemMapper voteSubjectItemMapper;

	@Override
	public List<VoteSubject> getVoteSubjectList(Long voteId) {
		Vote vote = this.voteMapper.selectById(voteId);
		Assert.notNull(vote, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("voteId", voteId));

		List<VoteSubject> subjects = this.lambdaQuery().eq(VoteSubject::getVoteId, voteId)
				.orderByAsc(VoteSubject::getSortFlag).list();

		Map<Long, List<VoteSubjectItem>> itemsMap = new LambdaQueryChainWrapper<>(this.voteSubjectItemMapper)
				.eq(VoteSubjectItem::getVoteId, voteId).orderByAsc(VoteSubjectItem::getSortFlag).list().stream()
				.collect(Collectors.groupingBy(VoteSubjectItem::getSubjectId));

		vote.setSubjectList(subjects);
		subjects.forEach(subject -> {
			subject.setItemList(itemsMap.getOrDefault(subject.getSubjectId(), List.of()));
		});
		return subjects;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addVoteSubject(VoteSubject voteSubject) {
		Map<Long, VoteSubject> dbSubjects = this.lambdaQuery().eq(VoteSubject::getVoteId, voteSubject.getVoteId())
				.orderByAsc(VoteSubject::getSortFlag).list().stream()
				.collect(Collectors.toMap(VoteSubject::getSubjectId, s -> s));

		voteSubject.setSubjectId(IdUtils.getSnowflakeId());
		if (IdUtils.validate(voteSubject.getNextSubjectId())) {
			// 在指定主题后的所有主题排序号+1
			VoteSubject nextSubject = dbSubjects.get(voteSubject.getNextSubjectId());
			dbSubjects.values().stream().filter(s -> s.getSortFlag() >= nextSubject.getSortFlag()).forEach(s -> {
				this.lambdaUpdate().set(VoteSubject::getSortFlag, s.getSortFlag() + 1)
						.eq(VoteSubject::getSubjectId, s.getSubjectId()).update();
			});
			voteSubject.setSortFlag(nextSubject.getSortFlag());
		} else {
			voteSubject.setSortFlag(dbSubjects.size());
		}
		this.save(voteSubject);
	}

	@Override
	public void updateVoteSubject(VoteSubject voteSubject) {
		VoteSubject db = this.getById(voteSubject.getSubjectId());
		Assert.notNull(db,
				() -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("subjectId", voteSubject.getSubjectId()));

		db.setTitle(voteSubject.getTitle());
		if (!db.getType().equals(voteSubject.getType())) {
			db.setType(voteSubject.getType());
			if (VoteSubjectType.isInput(db.getType())) {
				// 类型修改成输入则移除所有选项
				this.voteSubjectItemMapper.delete(new LambdaQueryChainWrapper<>(this.voteSubjectItemMapper)
						.eq(VoteSubjectItem::getSubjectId, db.getSubjectId()));
			}
		}
		this.updateById(db);
	}

	@Override
	public void deleteVoteSubjects(@NotEmpty List<Long> subjectIds) {
		this.removeByIds(subjectIds);
		// 删除选项
		this.voteSubjectItemMapper.delete(new LambdaQueryChainWrapper<>(this.voteSubjectItemMapper)
				.in(VoteSubjectItem::getSubjectId, subjectIds));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveSubjectItems(SaveSubjectItemsDTO dto) {
		VoteSubject subject = this.getById(dto.getSubjectId());
		Assert.notNull(subject, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("subjectId", dto.getSubjectId()));

		List<VoteSubjectItem> dbItems = new LambdaQueryChainWrapper<>(this.voteSubjectItemMapper)
				.eq(VoteSubjectItem::getSubjectId, subject.getSubjectId()).list();

		List<Long> updateItemIds = dto.getItemList().stream().filter(item -> IdUtils.validate(item.getItemId()))
				.map(VoteSubjectItem::getItemId).toList();
		List<Long> remmoveItemIds = dbItems.stream().filter(item -> !updateItemIds.contains(item.getItemId()))
				.map(VoteSubjectItem::getSubjectId).toList();
		if (remmoveItemIds.size() > 0) {
			this.voteSubjectItemMapper.deleteBatchIds(remmoveItemIds);
		}

		Map<Long, VoteSubjectItem> updateMap = dbItems.stream().filter(item -> updateItemIds.contains(item.getItemId()))
				.collect(Collectors.toMap(VoteSubjectItem::getItemId, item -> item));
		List<VoteSubjectItem> itemList = dto.getItemList();
		for (int i = 0; i < itemList.size(); i++) {
			VoteSubjectItem item = itemList.get(i);
			item.setSortFlag(i);
			if (IdUtils.validate(item.getItemId())) {
				VoteSubjectItem dbItem = updateMap.get(item.getItemId());
				BeanUtils.copyProperties(item, dbItem, "total");
				dbItem.updateBy(dto.getOperator().getUsername());
				this.voteSubjectItemMapper.updateById(dbItem);
			} else {
				item.setItemId(IdUtils.getSnowflakeId());
				item.setVoteId(subject.getVoteId());
				item.setSubjectId(subject.getSubjectId());
				item.setTotal(0);
				item.updateBy(dto.getOperator().getUsername());
				this.voteSubjectItemMapper.insert(item);
			}
		}
	}
}