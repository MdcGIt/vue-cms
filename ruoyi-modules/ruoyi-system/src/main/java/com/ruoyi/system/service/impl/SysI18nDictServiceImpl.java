package com.ruoyi.system.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.system.domain.SysI18nDict;
import com.ruoyi.system.mapper.SysI18nDictMapper;
import com.ruoyi.system.service.ISysI18nDictService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SysI18nDictServiceImpl extends ServiceImpl<SysI18nDictMapper, SysI18nDict> implements ISysI18nDictService {

	private final static String CACHE_PREFIX = "i18n:";
	
	private final RedisCache redisCache;

	@Override
	public String getLangValue(String languageTag, String langKey) {
		return redisCache.getCacheMapValue(CACHE_PREFIX + languageTag, langKey);
	}

	@Override
	public List<SysI18nDict> listByLangKey(String langKey) {
		return this.list(new LambdaQueryWrapper<SysI18nDict>().eq(SysI18nDict::getLangKey, langKey));
	}

	@Override
	public void insertI18nDict(SysI18nDict dict) {
		Assert.isTrue(this.checkUnique(dict), () -> CommonErrorCode.DATA_CONFLICT.exception(dict.getLangTag() + ":" + dict.getLangKey()));

		this.save(dict);
		redisCache.setCacheMapValue(CACHE_PREFIX + dict.getLangTag(), dict.getLangKey(), dict.getLangValue());
	}

	@Override
	public void updateI18nDict(SysI18nDict dict) {
		SysI18nDict db = this.getById(dict.getDictId());
		Assert.notNull(db, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("dictId", dict.getDictId()));
		Assert.isTrue(this.checkUnique(dict), () -> CommonErrorCode.DATA_CONFLICT.exception(dict.getLangTag() + ":" + dict.getLangKey()));

		this.updateById(dict);
		redisCache.setCacheMapValue(CACHE_PREFIX + dict.getLangTag(), dict.getLangKey(), dict.getLangValue());
	}

	@Override
	public void deleteI18nDictByIds(List<Long> dictIds) {
		List<SysI18nDict> list = this.listByIds(dictIds);
		this.removeBatchByIds(list);
		
		list.forEach(dict -> {
			redisCache.deleteCacheMapValue(CACHE_PREFIX + dict.getLangTag(), dict.getLangKey());
		});
	}
	
	private boolean checkUnique(SysI18nDict dict) {
		long count = this.count(new LambdaQueryWrapper<SysI18nDict>()
				.eq(SysI18nDict::getLangTag, dict.getLangTag())
				.eq(SysI18nDict::getLangKey, dict.getLangKey())
				.ne(IdUtils.validate(dict.getDictId()), SysI18nDict::getDictId, dict.getDictId()));
		return count == 0;
	}

	@Override
	public void batchSaveI18nDicts(List<SysI18nDict> dicts) {
		if (CollectionUtils.isEmpty(dicts)) {
			return;
		}
		for (SysI18nDict dict : dicts) {
			boolean checkUnique = this.checkUnique(dict);
			Assert.isTrue(checkUnique, () -> CommonErrorCode.DATA_CONFLICT.exception(dict.getLangTag() + ":" + dict.getLangKey()));
		}
		this.saveOrUpdateBatch(dicts);
		
		dicts.forEach(dict -> {
			redisCache.setCacheMapValue(CACHE_PREFIX + dict.getLangTag(), dict.getLangKey(), dict.getLangValue());
		});
	}
	
	@PostConstruct
	public void init() {
		this.resetCache();
	}

	@Override
	public void resetCache() {
		this.redisCache.keys(CACHE_PREFIX + "**").forEach(key -> redisCache.deleteObject(key));
		Map<String, List<SysI18nDict>> map = this.list().stream()
				.collect(Collectors.groupingBy(SysI18nDict::getLangTag));
		map.entrySet().stream().forEach(e -> {
			String langId = e.getKey();
			Map<String, String> kv = e.getValue().stream()
					.collect(Collectors.toMap(SysI18nDict::getLangKey, SysI18nDict::getLangValue));
			redisCache.setCacheMap(CACHE_PREFIX + langId, kv);
		});
	}
}