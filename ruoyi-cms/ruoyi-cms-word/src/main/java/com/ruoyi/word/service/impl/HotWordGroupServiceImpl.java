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
import com.ruoyi.word.domain.CmsHotWord;
import com.ruoyi.word.domain.CmsHotWordGroup;
import com.ruoyi.word.exception.WordErrorCode;
import com.ruoyi.word.mapper.CmsHotWordGroupMapper;
import com.ruoyi.word.mapper.CmsHotWordMapper;
import com.ruoyi.word.service.IHotWordGroupService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotWordGroupServiceImpl extends ServiceImpl<CmsHotWordGroupMapper, CmsHotWordGroup>
		implements IHotWordGroupService {

	private final CmsHotWordMapper hotWordMapper;

	@Override
	public List<CmsHotWordGroup> getHotWordGroupsBySiteId(Long siteId) {
		List<CmsHotWordGroup> list = this
				.list(new LambdaQueryWrapper<CmsHotWordGroup>().eq(CmsHotWordGroup::getSiteId, siteId));
		return list;
	}

	@Override
	public void addHotWordGroup(CmsHotWordGroup group) {
		this.checkUnique(group.getSiteId(), group.getGroupId(), group.getName(), group.getCode());
		
		group.setGroupId(IdUtils.getSnowflakeId());
		group.setSortFlag(SortUtils.getDefaultSortValue());
		group.createBy(StpAdminUtil.getLoginUser().getUsername());
		this.save(group);
	}

	@Override
	public void updateHotWordGroup(CmsHotWordGroup group) {
		CmsHotWordGroup db = this.getById(group.getGroupId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("groupId", group.getGroupId()));
		
		this.checkUnique(group.getSiteId(), group.getGroupId(), group.getName(), group.getCode());
		
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
			this.hotWordMapper.delete(new LambdaQueryWrapper<CmsHotWord>().eq(CmsHotWord::getGroupId, groupId));
		}
	}

	private void checkUnique(Long siteId, Long groupId, String name, String code) {
		Long count = this.lambdaQuery().eq(CmsHotWordGroup::getSiteId, siteId)
				.ne(groupId != null && groupId > 0, CmsHotWordGroup::getGroupId, groupId)
				.and(wrapper -> wrapper.eq(CmsHotWordGroup::getName, name).or().eq(CmsHotWordGroup::getCode, code))
				.count();
		Assert.isTrue(count == 0, WordErrorCode.CONFLIECT_HOT_WORD_GROUP::exception);
	}
}
