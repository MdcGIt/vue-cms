package com.ruoyi.common.security;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.ruoyi.common.redis.RedisCache;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.util.SaFoxUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaTokenDaoRedisImpl implements SaTokenDao {
	
	private final RedisCache redisCache;

	@Override
	public String get(String key) {
		return this.redisCache.getCacheObject(key);
	}

	@Override
	public void set(String key, String value, long timeout) {
		if (timeout > 0) {
			this.redisCache.setCacheObject(key, value, timeout, TimeUnit.SECONDS);
		} else if (timeout == SaTokenDao.NEVER_EXPIRE) {
			this.redisCache.setCacheObject(key, value);
		}
	}

	/**
	 * 过期时间不变
	 */
	@Override
	public void update(String key, String value) {
		long expire = getTimeout(key);
		if(expire == NOT_VALUE_EXPIRE) {
			return;
		}
		this.set(key, value, expire);
	}

	@Override
	public void delete(String key) {
		this.redisCache.deleteObject(key);
	}

	@Override
	public long getTimeout(String key) {
		return this.redisCache.getExpire(key, TimeUnit.SECONDS);
	}

	@Override
	public void updateTimeout(String key, long timeout) {
		if (timeout > 0) {
			this.redisCache.expire(key, timeout);
		} else if (timeout == NEVER_EXPIRE) {
			long expire = this.getTimeout(key);
			if (expire != NEVER_EXPIRE) {
				this.redisCache.expire(key, timeout, TimeUnit.SECONDS);
			}
		}
	}

	@Override
	public Object getObject(String key) {
		return this.redisCache.getCacheObject(key);
	}

	@Override
	public void setObject(String key, Object object, long timeout) {
		if (timeout > 0) {
			this.redisCache.setCacheObject(key, object, timeout, TimeUnit.SECONDS);
		} else if (timeout == SaTokenDao.NEVER_EXPIRE) {
			this.redisCache.setCacheObject(key, object);
		}
	}

	/**
	 * 过期时间不变
	 */
	@Override
	public void updateObject(String key, Object object) {
		long expire = this.getObjectTimeout(key);
		if(expire == NOT_VALUE_EXPIRE) {
			return;
		}
		this.setObject(key, object, expire);
	}

	@Override
	public void deleteObject(String key) {
		this.redisCache.deleteObject(key);
		
	}

	@Override
	public long getObjectTimeout(String key) {
		return this.redisCache.getExpire(key, TimeUnit.SECONDS);
	}

	@Override
	public void updateObjectTimeout(String key, long timeout) {
		if (timeout > 0) {
			this.redisCache.expire(key, timeout);
		} else if (timeout == NEVER_EXPIRE) {
			long expire = this.getObjectTimeout(key);
			if (expire != NEVER_EXPIRE) {
				this.redisCache.expire(key, timeout, TimeUnit.SECONDS);
			}
		}
	}

	@Override
	public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
		List<String> keys = this.redisCache.scanKeys(prefix + "*" + keyword + "*", 1000).stream().toList();
		return SaFoxUtil.searchList(keys, start, size, sortType);
	}

}
