package com.ruoyi.advertisement.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.advertisement.domain.CmsAdDayStat;
import com.ruoyi.advertisement.mapper.CmsAdDayStatMapper;
import com.ruoyi.advertisement.service.impl.AdvertisementStatServiceImpl;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.IdUtils;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 广告统计任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdvertisementStatJob extends IJobHandler {

	static final String JOB_NAME = "AdvertisementStatJob";

	private final CmsAdDayStatMapper dayStatMapper;
	
	private final RedisCache redisCache;

	/**
	 * 默认每5分钟执行一次，更新广告日点击数和展现数到统计表中
	 */
	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		log.info("AdvertisementStatJob start");
		long s = System.currentTimeMillis();
		// 当日数据更新
		String day = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		this.saveToDb(day, false);
		// 尝试更新昨日数据并删除cache
		String yestoday = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		this.saveToDb(yestoday, true);
		log.info("AdvertisementPublishJob completed, cost: {}ms", System.currentTimeMillis() - s);
	}
	
	private void saveToDb(String day, boolean deleteCache) {
		String cacheKey = AdvertisementStatServiceImpl.CACHE_PREFIX + day;
		Map<String, Integer> clickMap = this.redisCache.hasKey(cacheKey + "-click") ? this.redisCache.getCacheMap(cacheKey + "-click") : new HashMap<>();
		Map<String, Integer> viewMap = this.redisCache.hasKey(cacheKey + "-view") ? this.redisCache.getCacheMap(cacheKey + "-view") : new HashMap<>();

		if (clickMap.size() == 0 && viewMap.size() == 0) {
			return;
		}

		Map<Long, CmsAdDayStat> stats = new LambdaQueryChainWrapper<>(this.dayStatMapper).eq(CmsAdDayStat::getDay, day).list().stream()
				.collect(Collectors.toMap(CmsAdDayStat::getAdvertisementId, stat -> stat));
		// 点击数
		clickMap.entrySet().forEach(e -> {
			Long advId = Long.valueOf(e.getKey());
			CmsAdDayStat stat = stats.get(advId);
			if (Objects.nonNull(stat)) {
				stat = new CmsAdDayStat();
				stat.setDay(day);
				stat.setAdvertisementId(advId);
				stat.setClick(0);
				stat.setView(0);
				stats.put(advId, stat);
			}
			stat.setClick(e.getValue());
		});
		// 展现数
		viewMap.entrySet().forEach(e -> {
			Long advId = Long.valueOf(e.getKey());
			CmsAdDayStat stat = stats.get(advId);
			if (Objects.nonNull(stat)) {
				stat = new CmsAdDayStat();
				stat.setDay(day);
				stat.setAdvertisementId(advId);
				stat.setClick(0);
				stat.setView(0);
				stats.put(advId, stat);
			}
			stat.setView(e.getValue());
		});
		// 更新数据库
		stats.values().forEach(stat -> {
			if (IdUtils.validate(stat.getStatId())) {
				this.dayStatMapper.updateById(stat);
			} else {
				this.dayStatMapper.insert(stat);
			}
		});
		if (deleteCache) {
			this.redisCache.deleteObject(cacheKey + "-click");
			this.redisCache.deleteObject(cacheKey + "-view");
		}
	}
}
