package com.ruoyi.common.redis;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.KeyScanOptions;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.StringUtils;

/**
 * spring redis 工具类
 **/
@Component
@SuppressWarnings(value = { "unchecked", "rawtypes" })
public class RedisCache {

	public final RedisTemplate redisTemplate;

	/**
	 * 是否允许缓存null
	 */
	private boolean allowNullValue = true;

	/**
	 * null值缓存默认过期时间30秒
	 */
	private long nullValueExpire = 30;

	public RedisCache(RedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 缓存基本的对象，Integer、String、实体类等
	 *
	 * @param key   缓存的键值
	 * @param value 缓存的值
	 */
	public <T> void setCacheObject(final String key, final T value) {
		if (Objects.isNull(value) && !this.allowNullValue) {
			return;
		}
		if (Objects.isNull(value)) {
			redisTemplate.opsForValue().set(key, value, this.nullValueExpire, TimeUnit.SECONDS);
		} else {
			redisTemplate.opsForValue().set(key, value);
		}
	}

	/**
	 * 缓存基本的对象，Integer、String、实体类等
	 *
	 * @param key      缓存的键值
	 * @param value    缓存的值
	 * @param timeout  时间
	 * @param timeUnit 时间颗粒度
	 */
	public <T> void setCacheObject(final String key, final T value, final long timeout, final TimeUnit timeUnit) {
		if (Objects.isNull(value) && !this.allowNullValue) {
			return;
		}
		if (Objects.isNull(value)) {
			redisTemplate.opsForValue().set(key, value, this.nullValueExpire, TimeUnit.SECONDS);
		} else {
			redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
		}
	}

	/**
	 * 设置有效时间
	 *
	 * @param key     Redis键
	 * @param timeout 超时时间
	 * @return true=设置成功；false=设置失败
	 */
	public boolean expire(final String key, final long timeout) {
		return expire(key, timeout, TimeUnit.SECONDS);
	}

	/**
	 * 设置有效时间
	 *
	 * @param key     Redis键
	 * @param timeout 超时时间
	 * @param unit    时间单位
	 * @return true=设置成功；false=设置失败
	 */
	public boolean expire(final String key, final long timeout, final TimeUnit unit) {
		return redisTemplate.expire(key, timeout, unit);
	}

	/**
	 * 获取有效时间
	 *
	 * @param key Redis键
	 * @return 有效时间
	 */
	public long getExpire(final String key) {
		return redisTemplate.getExpire(key);
	}

	public long getExpire(final String key, final TimeUnit unit) {
		return redisTemplate.getExpire(key, unit);
	}

	/**
	 * 判断 key是否存在
	 *
	 * @param key 键
	 * @return true 存在 false不存在
	 */
	public Boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}

	/**
	 * 获得缓存的基本对象。
	 *
	 * @param key 缓存键值
	 * @return 缓存键值对应的数据
	 */
	public <T> T getCacheObject(final String key) {
		ValueOperations<String, T> operation = redisTemplate.opsForValue();
		return operation.get(key);
	}
	
	/**
	 * 获得缓存的基本对象，如果不存在调用supplier获取数据。
	 * 
	 * @param <T>
	 * @param cacheKey
	 * @param supplier
	 * @return
	 */
	public <T> T getCacheObject(String cacheKey, Supplier<T> supplier) {
		T cacheObject = this.getCacheObject(cacheKey);
		if (Objects.isNull(cacheObject)) {
			cacheObject = supplier.get();
			if (Objects.nonNull(cacheObject)) {
				this.setCacheObject(cacheKey, cacheObject);
			}
		}
		return cacheObject;
	} 

	/**
	 * 删除单个对象
	 *
	 * @param key
	 */
	public boolean deleteObject(final String key) {
		return redisTemplate.delete(key);
	}

	/**
	 * 删除集合对象
	 *
	 * @param collection 多个对象
	 * @return
	 */
	public boolean deleteObject(final Collection collection) {
		return redisTemplate.delete(collection) > 0;
	}

	/**
	 * 缓存List数据
	 *
	 * @param key      缓存的键值
	 * @param dataList 待缓存的List数据
	 * @return 缓存的对象
	 */
	public <T> long setCacheList(final String key, final List<T> dataList) {
		boolean empty = StringUtils.isEmpty(dataList);
		if (empty && !this.allowNullValue) {
			return 0;
		}
		if (empty) {
			redisTemplate.opsForValue().set(key, Collections.emptyList(), this.nullValueExpire, TimeUnit.SECONDS);
			return 0;
		} else {
			Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
			return count == null ? 0 : count;
		}
	}

	/**
	 * 获得缓存的list对象
	 *
	 * @param key 缓存的键值
	 * @return 缓存键值对应的数据
	 */
	public <T> List<T> getCacheList(final String key) {
		return redisTemplate.opsForList().range(key, 0, -1);
	}

	/**
	 * 缓存Set
	 *
	 * @param key     缓存键值
	 * @param dataSet 缓存的数据
	 * @return 缓存数据的对象
	 */
	public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet) {
		BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
		boolean empty = StringUtils.isEmpty(dataSet);
		if (empty && this.allowNullValue) {
			setOperation.expire(nullValueExpire, TimeUnit.SECONDS);
		}
		if (!empty) {
			Iterator<T> it = dataSet.iterator();
			while (it.hasNext()) {
				setOperation.add(it.next());
			}
		}
		return setOperation;
	}

	/**
	 * 获得缓存的set
	 *
	 * @param key
	 * @return
	 */
	public <T> Set<T> getCacheSet(final String key) {
		return redisTemplate.opsForSet().members(key);
	}

	/**
	 * 缓存Map
	 *
	 * @param key
	 * @param dataMap
	 */
	public <T> void setCacheMap(final String key, final Map<String, T> dataMap) {
		if (dataMap != null) {
			redisTemplate.opsForHash().putAll(key, dataMap);
		}

		boolean empty = StringUtils.isEmpty(dataMap);
		if (empty && !this.allowNullValue) {
			return;
		}
		if (empty) {
			BoundHashOperations ops = redisTemplate.boundHashOps(key);
			ops.expire(nullValueExpire, TimeUnit.SECONDS);
		} else {
			redisTemplate.opsForHash().putAll(key, dataMap);
		}
	}

	/**
	 * 获得缓存的Map
	 *
	 * @param key
	 * @return
	 */
	public <T> Map<String, T> getCacheMap(final String key) {
		return redisTemplate.opsForHash().entries(key);
	}

	public <T> Map<String, T> getCacheMap(final String key, Supplier<Map<String, T>> supplier) {
		Map entries = redisTemplate.opsForHash().entries(key);
		if (entries == null) {
			entries = supplier.get();
			if (entries != null) {
				this.setCacheMap(key, entries);
			}
		}
		return entries;
	}

	/**
	 * 往Hash中存入数据
	 *
	 * @param key   Redis键
	 * @param hKey  Hash键
	 * @param value 值
	 */
	public <T> void setCacheMapValue(final String key, final String hKey, final T value) {
		redisTemplate.opsForHash().put(key, hKey, value);
	}

	/**
	 * 获取Hash中的数据
	 *
	 * @param key  Redis键
	 * @param hKey Hash键
	 * @return Hash中的对象
	 */
	public <T> T getCacheMapValue(final String key, final String hKey) {
		HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
		return opsForHash.get(key, hKey);
	}
	
	public <T> T getCacheMapValue(final String key, final String hKey, Supplier<T> supplier) {
		T cacheMapValue = this.getCacheMapValue(key, hKey);
		if (cacheMapValue == null) {
			cacheMapValue = supplier.get();
			if (cacheMapValue != null) {
				this.setCacheMapValue(key, hKey, cacheMapValue);
			}
		}
		return cacheMapValue;
	}

	/**
	 * 获取多个Hash中的数据
	 *
	 * @param key   Redis键
	 * @param hKeys Hash键集合
	 * @return Hash对象集合
	 */
	public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys) {
		return redisTemplate.opsForHash().multiGet(key, hKeys);
	}

	/**
	 * 删除Hash中的某条数据
	 *
	 * @param key  Redis键
	 * @param hKey Hash键
	 * @return 是否成功
	 */
	public boolean deleteCacheMapValue(final String key, final String hKey) {
		return redisTemplate.opsForHash().delete(key, hKey) > 0;
	}

	/**
	 * 获得缓存的基本对象列表
	 *
	 * @param pattern 字符串前缀
	 * @return 对象列表
	 */
	public Collection<String> keys(final String pattern) {
		return redisTemplate.keys(pattern);
	}

	/**
	 * 获得匹配的n条缓存键名列表
	 *
	 * @param pattern
	 * @return
	 */
	public Set<String> scanKeys(final String pattern, final int count) {
		return scanKeys(DataType.STRING, pattern, count);
	}

	/**
	 * 获得匹配的n条缓存键名列表
	 *
	 * @param dataType
	 * @param pattern
	 * @param count
	 * @return
	 */
	public Set<String> scanKeys(DataType dataType, final String pattern, int count) {
		if (Objects.isNull(dataType) || DataType.NONE.equals(dataType)) {
			return Set.of();
		}
		return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
			Set<String> keysTmp = new HashSet<>();
			KeyScanOptions options = (KeyScanOptions) KeyScanOptions.scanOptions(dataType).match(pattern).count(count).build();
			Cursor<byte[]> cursor = connection.scan(options);
			while (cursor.hasNext()) {
				keysTmp.add(new String(cursor.next(), StandardCharsets.UTF_8));
			}
			return keysTmp;
		});
	}

	/**
	 * 获得自增ID
	 *
	 * @param key
	 * @return
	 */
	public long atomicLongIncr(String key) {
		RedisAtomicLong redisAtomicLong = new RedisAtomicLong(key, this.redisTemplate);
		return redisAtomicLong.incrementAndGet();
	}
	
	/**
	 * 计数+delta
	 * 
	 * @param key
	 * @param value
	 * @param delta
	 * @return
	 */
	public Long incrMapValue(String cacheKey, String mapKey, int delta) {
		return this.redisTemplate.opsForHash().increment(cacheKey, mapKey, delta);
	}
}
