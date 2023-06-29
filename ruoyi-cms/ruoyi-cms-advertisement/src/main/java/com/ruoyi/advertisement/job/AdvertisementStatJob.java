package com.ruoyi.advertisement.job;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ruoyi.system.schedule.IScheduledHandler;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.ruoyi.advertisement.domain.CmsAdHourStat;
import com.ruoyi.advertisement.domain.CmsAdvertisement;
import com.ruoyi.advertisement.mapper.CmsAdvertisementMapper;
import com.ruoyi.advertisement.service.IAdHourStatService;
import com.ruoyi.advertisement.service.impl.AdvertisementStatServiceImpl;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.IdUtils;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 广告统计任务
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RequiredArgsConstructor
@Component(IScheduledHandler.BEAN_PREFIX + AdvertisementStatJob.JOB_NAME)
public class AdvertisementStatJob extends IJobHandler implements IScheduledHandler {

	static final String JOB_NAME = "AdvertisementStatJob";

	private final IAdHourStatService adStatService;

	private final CmsAdvertisementMapper advMapper;

	private final RedisCache redisCache;

	@Override
	public String getId() {
		return JOB_NAME;
	}

	@Override
	public String getName() {
		return "{SCHEDULED_TASK." + JOB_NAME + "}";
	}

	@Override
	public void exec() throws Exception {
		log.info("AdvertisementStatJob start");
		long s = System.currentTimeMillis();
		try {
			// 数据更新
			String hour = LocalDateTime.now().format(AdvertisementStatServiceImpl.DATE_TIME_FORMAT);
			this.saveToDb(hour, false);
			// 尝试更新上一个小时数据并删除cache
			String yestoday = LocalDateTime.now().minusHours(1).format(AdvertisementStatServiceImpl.DATE_TIME_FORMAT);
			this.saveToDb(yestoday, true);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		log.info("AdvertisementPublishJob completed, cost: {}ms", System.currentTimeMillis() - s);
	}

	private void saveToDb(String hour, boolean deleteCache) {
		String clickCacheKey = AdvertisementStatServiceImpl.CLIC_CACHE_PREFIX + hour;
		String viewCacheKey = AdvertisementStatServiceImpl.VIEW_CACHE_PREFIX + hour;

		Map<Long, CmsAdHourStat> stats = this.adStatService.lambdaQuery().eq(CmsAdHourStat::getHour, hour).list()
				.stream().collect(Collectors.toMap(CmsAdHourStat::getAdvertisementId, stat -> stat));

		Map<Long, Long> advertisements = new LambdaQueryChainWrapper<>(this.advMapper)
				.select(CmsAdvertisement::getAdvertisementId, CmsAdvertisement::getSiteId).list().stream()
				.collect(Collectors.toMap(CmsAdvertisement::getAdvertisementId, CmsAdvertisement::getSiteId));

		List<Long> insertAdvIds = new ArrayList<>();
		for (Long advertisementId : advertisements.keySet()) {
			int click = this.redisCache.getZsetScore(clickCacheKey, advertisementId.toString()).intValue();
			int view = this.redisCache.getZsetScore(viewCacheKey, advertisementId.toString()).intValue();
			if (click > 0 || view > 0) {
				CmsAdHourStat stat = stats.get(advertisementId);
				if (Objects.isNull(stat)) {
					stat = new CmsAdHourStat();
					stat.setSiteId(advertisements.get(advertisementId));
					stat.setHour(hour);
					stat.setAdvertisementId(advertisementId);

					stats.put(advertisementId, stat);
					insertAdvIds.add(advertisementId);
				}
				stat.setClick(click > 0 ? click : 0);
				stat.setView(view > 0 ? view : 0);
			}
		}
		// 更新数据库
		List<CmsAdHourStat> inserts = stats.values().stream()
				.filter(stat -> insertAdvIds.contains(stat.getAdvertisementId())).toList();
		this.adStatService.saveBatch(inserts);
		List<CmsAdHourStat> updates = stats.values().stream()
				.filter(stat -> !insertAdvIds.contains(stat.getAdvertisementId())).toList();
		this.adStatService.updateBatchById(updates);
		// 清理过期缓存
		if (deleteCache) {
			this.redisCache.deleteObject(clickCacheKey);
			this.redisCache.deleteObject(viewCacheKey);
		}
	}

	@Override
	@XxlJob(JOB_NAME)
	public void execute() throws Exception {
		this.exec();
	}
}
