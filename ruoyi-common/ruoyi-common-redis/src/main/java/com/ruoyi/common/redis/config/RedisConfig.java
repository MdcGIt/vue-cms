package com.ruoyi.common.redis.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;

import lombok.RequiredArgsConstructor;

/**
 * redis配置
 * 
 * @author ruoyi
 */
@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig implements CachingConfigurer {

	private final CacheProperties cacheProperties;

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);

		ObjectMapper objectMapper = JacksonUtils.newObjectMapper();
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
				JsonTypeInfo.As.PROPERTY);

		Jackson2JsonRedisSerializer<Object> redisValueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
		redisTemplate.setDefaultSerializer(redisValueSerializer);

		redisTemplate.setKeySerializer(RedisSerializer.string());
		redisTemplate.setHashKeySerializer(RedisSerializer.string());
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}
	
	/**
	 * 使用@Cacheable等缓存注解的时候默认缓存的值是二进制的，改成JSON格式的
	 */
	@Bean
	public RedisCacheConfiguration redisCacheConfiguration(RedisTemplate<String, Object> redisTemplate) {
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
				.serializeValuesWith(
						RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getValueSerializer()));
		if (cacheProperties.getRedis().getTimeToLive() != null) {
			redisCacheConfiguration = redisCacheConfiguration.entryTtl(cacheProperties.getRedis().getTimeToLive());
		} else {
			redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofHours(1)); // 缓存失效时间默认改成1小时
		}
		if (!cacheProperties.getRedis().isUseKeyPrefix()) {
			redisCacheConfiguration = redisCacheConfiguration.disableKeyPrefix();
		}
		if (redisCacheConfiguration.usePrefix() && StringUtils.isNotEmpty(cacheProperties.getRedis().getKeyPrefix())) {
			redisCacheConfiguration = redisCacheConfiguration.prefixCacheNameWith(cacheProperties.getRedis().getKeyPrefix());
		}
		if (!cacheProperties.getRedis().isCacheNullValues()) {
			redisCacheConfiguration = redisCacheConfiguration.disableCachingNullValues();
		}
		return redisCacheConfiguration;
	}
}
