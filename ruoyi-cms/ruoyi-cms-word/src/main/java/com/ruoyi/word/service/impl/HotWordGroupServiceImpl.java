package com.ruoyi.word.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.word.domain.CmsHotWord;
import com.ruoyi.word.domain.CmsHotWordGroup;
import com.ruoyi.word.mapper.CmsHotWordGroupMapper;
import com.ruoyi.word.service.IHotWordGroupService;
import com.ruoyi.word.service.IHotWordService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HotWordGroupServiceImpl extends ServiceImpl<CmsHotWordGroupMapper, CmsHotWordGroup> implements IHotWordGroupService {

	private final IHotWordService hotWordService;
	
	@Override
	public void deleteHotWordGroups(List<Long> groupIds) {
		for (Long groupId : groupIds) {
			this.removeById(groupId);
			this.hotWordService.remove(new LambdaQueryWrapper<CmsHotWord>().eq(CmsHotWord::getGroupId, groupId));
		}
	}

	@Override
	public List<CmsHotWordGroup> getHotWordGroupsBySiteId(Long siteId) {
		// TODO 缓存
		List<CmsHotWordGroup> list = this.list(new LambdaQueryWrapper<CmsHotWordGroup>()
    			.eq(CmsHotWordGroup::getSiteId, siteId));
		return list;
	}
}
