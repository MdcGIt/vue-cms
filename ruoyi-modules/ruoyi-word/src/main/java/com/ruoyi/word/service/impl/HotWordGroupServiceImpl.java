package com.ruoyi.word.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.SortUtils;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.word.domain.HotWord;
import com.ruoyi.word.domain.HotWordGroup;
import com.ruoyi.word.exception.WordErrorCode;
import com.ruoyi.word.mapper.HotWordGroupMapper;
import com.ruoyi.word.mapper.HotWordMapper;
import com.ruoyi.word.service.IHotWordGroupService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotWordGroupServiceImpl extends ServiceImpl<HotWordGroupMapper, HotWordGroup>
		implements IHotWordGroupService {

	private final HotWordMapper hotWordMapper;

	@Override
	public HotWordGroup addHotWordGroup(HotWordGroup group) {
		this.checkUnique(group.getGroupId(), group.getName(), group.getCode());

		group.setGroupId(IdUtils.getSnowflakeId());
		group.setSortFlag(SortUtils.getDefaultSortValue());
		group.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.save(group);
		return group;
	}

	@Override
	public void updateHotWordGroup(HotWordGroup group) {
		HotWordGroup db = this.getById(group.getGroupId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("groupId", group.getGroupId()));

		this.checkUnique(group.getGroupId(), group.getName(), group.getCode());

		db.setName(group.getName());
		db.setCode(group.getCode());
		db.setRemark(group.getRemark());
		db.updateBy(group.getUpdateBy());
		this.updateById(db);
	}

	@Override
	public void deleteHotWordGroups(List<Long> groupIds) {
		for (Long groupId : groupIds) {
			this.removeById(groupId);
			this.hotWordMapper.delete(new LambdaQueryWrapper<HotWord>().eq(HotWord::getGroupId, groupId));
		}
	}

	private void checkUnique(Long groupId, String name, String code) {
		Long count = this.lambdaQuery().ne(groupId != null && groupId > 0, HotWordGroup::getGroupId, groupId)
				.and(wrapper -> wrapper.eq(HotWordGroup::getName, name).or().eq(HotWordGroup::getCode, code))
				.count();
		Assert.isTrue(count == 0, WordErrorCode.CONFLIECT_HOT_WORD_GROUP::exception);
	}
}
