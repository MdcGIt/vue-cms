package com.ruoyi.cms.stat.service.impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ruoyi.cms.stat.domain.CmsSiteVisitLog;
import com.ruoyi.cms.stat.mapper.CmsSiteVisitLogMapper;
import com.ruoyi.cms.stat.service.ICmsStatService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CmsStatServiceImpl implements ICmsStatService {

	private final CmsSiteVisitLogMapper siteVisitLogMapper;
	
	@Async
	@Override
	public void addSiteVisitLog(CmsSiteVisitLog log) {
		// TODO 重复访问验证策略
		this.siteVisitLogMapper.insert(log);
	}
}
