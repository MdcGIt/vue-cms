package com.ruoyi.link.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.link.domain.CmsLink;
import com.ruoyi.link.domain.CmsLinkGroup;
import com.ruoyi.link.mapper.CmsLinkGroupMapper;
import com.ruoyi.link.service.ILinkGroupService;
import com.ruoyi.link.service.ILinkService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LinkGroupServiceImpl extends ServiceImpl<CmsLinkGroupMapper, CmsLinkGroup> implements ILinkGroupService {
	
	private final ILinkService linkService;
	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteLinkGroup(List<Long> linkGroupIds) {
		for (Long linkGroupId : linkGroupIds) {
			this.removeById(linkGroupId);
			this.linkService.remove(new LambdaQueryWrapper<CmsLink>().eq(CmsLink::getGroupId, linkGroupId));
		}
		return false;
	}

}
