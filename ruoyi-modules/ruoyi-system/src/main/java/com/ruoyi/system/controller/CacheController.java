package com.ruoyi.system.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.redis.IMonitoredCache;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysCache;
import com.ruoyi.system.permission.SysMenuPriv;
import com.ruoyi.system.security.AdminUserType;

import lombok.RequiredArgsConstructor;

/**
 * 缓存监控
 * 
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/monitor/cache")
public class CacheController {

	private final RedisTemplate<String, String> redisTemplate;
	
	private final List<IMonitoredCache> monitoredCaches;

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheList)
	@GetMapping
	public R<?> getInfo() throws Exception {
		Properties info = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.serverCommands().info());
		Properties commandStats = (Properties) redisTemplate
				.execute((RedisCallback<Object>) connection -> connection.serverCommands().info("commandstats"));
		Object dbSize = redisTemplate.execute((RedisCallback<Object>) connection -> connection.serverCommands().dbSize());

		Map<String, Object> result = new HashMap<>(3);
		result.put("info", info);
		result.put("dbSize", dbSize);

		List<Map<String, String>> pieList = new ArrayList<>();
		commandStats.stringPropertyNames().forEach(key -> {
			Map<String, String> data = new HashMap<>(2);
			String property = commandStats.getProperty(key);
			data.put("name", StringUtils.removeStart(key, "cmdstat_"));
			data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
			pieList.add(data);
		});
		result.put("commandStats", pieList);
		return R.ok(result);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheList)
	@GetMapping("/getNames")
	public R<?> cache() {
		List<SysCache> list = this.monitoredCaches.stream().map(mc -> {
			return new SysCache(mc.getCacheKey(), mc.getCacheName());
		}).collect(Collectors.toList());
		return R.ok(list);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheList)
	@GetMapping("/getKeys/{cacheName}")
	public R<?> getCacheKeys(@PathVariable String cacheName) {
		Set<String> cacheKeys = redisTemplate.keys(cacheName + "*");
		return R.ok(cacheKeys);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheList)
	@GetMapping("/getValue/{cacheName}/{cacheKey}")
	public R<?> getCacheValue(@PathVariable String cacheName, @PathVariable String cacheKey) {
		String cacheValue = redisTemplate.opsForValue().get(cacheKey);
		SysCache sysCache = new SysCache(cacheName, cacheKey, cacheValue);
		sysCache.setExpireTime(redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS));
		return R.ok(sysCache);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheList)
	@DeleteMapping("/clearCacheName/{cacheName}")
	public R<?> clearCacheName(@PathVariable String cacheName) {
		Collection<String> cacheKeys = redisTemplate.keys(cacheName + "*");
		redisTemplate.delete(cacheKeys);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheList)
	@DeleteMapping("/clearCacheKey/{cacheKey}")
	public R<?> clearCacheKey(@PathVariable String cacheKey) {
		redisTemplate.delete(cacheKey);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.MonitorCacheList)
	@DeleteMapping("/clearCacheAll")
	public R<?> clearCacheAll() {
		Collection<String> cacheKeys = redisTemplate.keys("*");
		redisTemplate.delete(cacheKeys);
		return R.ok();
	}
}
