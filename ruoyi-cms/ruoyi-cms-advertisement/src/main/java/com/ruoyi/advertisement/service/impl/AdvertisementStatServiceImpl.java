package com.ruoyi.advertisement.service.impl;

import java.time.format.DateTimeFormatter;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ruoyi.advertisement.domain.CmsAdClickLog;
import com.ruoyi.advertisement.domain.CmsAdViewLog;
import com.ruoyi.advertisement.mapper.CmsAdClickLogMapper;
import com.ruoyi.advertisement.mapper.CmsAdViewLogMapper;
import com.ruoyi.advertisement.service.IAdvertisementStatService;
import com.ruoyi.common.redis.RedisCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdvertisementStatServiceImpl implements IAdvertisementStatService {
	
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

	public static final String CACHE_PREFIX = "adv:daystat-";
	
	private final CmsAdClickLogMapper clickLogMapper;
	
	private final CmsAdViewLogMapper viewLogMapper;
	
	private final RedisCache redisCache;

	@Async
	@Override
	public void adClick(CmsAdClickLog log) {
		// TODO 重复访问验证策略
		// redis 广告日点击数+1
		String cacheKey = CACHE_PREFIX + log.getEvtTime().format(DATE_TIME_FORMAT);
		redisCache.incrMapValue(cacheKey, log.getAdId().toString(), 1);
		// 记录点击日志
		this.clickLogMapper.insert(log);
	}

	@Async
	@Override
	public void adView(CmsAdViewLog log) {
		// TODO 重复访问验证策略
		// redis 广告日展现数+1
		String cacheKey = CACHE_PREFIX + log.getEvtTime().format(DATE_TIME_FORMAT);
		redisCache.incrMapValue(cacheKey, log.getAdId().toString(), 1);
		// 记录展现日志
		this.viewLogMapper.insert(log);
	}
}
