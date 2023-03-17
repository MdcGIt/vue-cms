package com.ruoyi.common.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.yitter.contract.IdGeneratorOptions;
import com.github.yitter.idgen.YitIdHelper;
import com.ruoyi.common.config.properties.RuoYiProperties;
import com.ruoyi.common.config.properties.RuoYiProperties.Snowflake;

/**
 * ID生成器工具类
 */
public class IdUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(IdUtils.class);
	
	static {
		RuoYiProperties properties = SpringUtils.getBean(RuoYiProperties.class);
		Snowflake snowflake = properties.getSnowflake();
		short workerId = Objects.isNull(snowflake) ? 1 : snowflake.getWorkerId();
		IdGeneratorOptions options = new IdGeneratorOptions(workerId);
		YitIdHelper.setIdGenerator(options);
		logger.info("Snowflake: workerId={}", workerId);
	}
	
	public static long getSnowflakeId() {
		return YitIdHelper.nextId();
	}

	public static String getSnowflakeIdStr() {
		return String.valueOf(getSnowflakeId());
	}
	
	/**
	 * 获取随机UUID
	 * 
	 * @return 随机UUID
	 */
	public static String randomUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 简化的UUID，去掉了横线
	 * 
	 * @return 简化的UUID，去掉了横线
	 */
	public static String simpleUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	/**
	 * 校验列表中的ID是否正确
	 * 
	 * @param ids
	 * @param removeInvalidId
	 *            是否移除错误ID
	 * @return
	 */
	public static boolean validate(List<Long> ids, boolean removeInvalidId) {
		if (ids == null || ids.size() == 0) {
			return false;
		}
		for (Iterator<Long> iterator = ids.iterator(); iterator.hasNext();) {
			Long id = iterator.next();
			if (id == null || id.longValue() <= 0) {
				if (removeInvalidId) {
					iterator.remove();
				} else {
					return false;
				}
			}
		}
		return ids.size() > 0;
	}

	public static boolean validate(List<Long> ids) {
		return validate(ids, false);
	}

	public static boolean validate(Long id) {
		return id != null && id.longValue() > 0;
	}
}
