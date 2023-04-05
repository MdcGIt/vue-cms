package com.ruoyi.system.service.impl;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.SysConstants;
import com.ruoyi.system.domain.SysDictData;
import com.ruoyi.system.domain.SysDictType;
import com.ruoyi.system.exception.SysErrorCode;
import com.ruoyi.system.fixed.FixedDictUtils;
import com.ruoyi.system.mapper.SysDictDataMapper;
import com.ruoyi.system.mapper.SysDictTypeMapper;
import com.ruoyi.system.service.ISysDictTypeService;

import lombok.RequiredArgsConstructor;

/**
 * 字典 业务层处理
 * 
 * @author ruoyi
 */
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType>
		implements ISysDictTypeService, CommandLineRunner {

	private final RedisCache redisCache;

	private final SysDictDataMapper dictDataMapper;

	@Override
	public List<SysDictData> selectDictDatasByType(String dictType) {
		List<SysDictData> dictDatas = this.redisCache.getCacheObject(SysConstants.CACHE_SYS_DICT_KEY + dictType, () -> {
			return new LambdaQueryChainWrapper<>(this.dictDataMapper).eq(SysDictData::getDictType, dictType).list();
		});
		return dictDatas;
	}

	@Override
	public Optional<SysDictData> optDictData(String dictType, String dictValue) {
		return this.selectDictDatasByType(dictType).stream().filter(d -> d.getDictValue().equals(dictValue))
				.findFirst();
	}

	@Override
	public void deleteDictTypeByIds(List<Long> dictIds) {
		List<SysDictType> dicts = this.listByIds(dictIds);
		for (SysDictType dictType : dicts) {
			// 不能删除系统固定字典数据
			Assert.isFalse(FixedDictUtils.isFixedDictType(dictType.getDictType()),
					CommonErrorCode.FIXED_DICT::exception);
			// 删除字典数据项
			this.dictDataMapper
					.delete(new LambdaQueryWrapper<SysDictData>().eq(SysDictData::getDictType, dictType.getDictType()));

			this.removeById(dictType);
			this.deleteCache(dictType.getDictType());
		}
	}

	@Override
	public void insertDictType(SysDictType dict) {
		Assert.isTrue(this.checkDictTypeUnique(dict),
				() -> SysErrorCode.DICT_TYPE_CONFLICT.exception(dict.getDictType()));
		dict.setCreateTime(LocalDateTime.now());
		this.save(dict);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateDictType(SysDictType dict) {
		SysDictType dbDict = this.getById(dict.getDictId());
		Assert.notNull(dbDict, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("dictId", dict.getDictId()));
		Assert.isTrue(this.checkDictTypeUnique(dict),
				() -> CommonErrorCode.DATA_CONFLICT.exception("dictType:" + dict.getDictType()));

		// 类型变更需要更新关联字典项数据
		String oldDictType = dbDict.getDictType();

		dbDict.setDictName(dict.getDictName());
		dbDict.setDictType(dict.getDictType());
		dbDict.setRemark(dict.getRemark());
		dbDict.setUpdateTime(LocalDateTime.now());
		this.updateById(dbDict);
		// 类型变更需要更新字典数据，更新缓存
		if (!oldDictType.equals(dbDict.getDictType())) {
			// 不能修改系统固定字典类型
			Assert.isFalse(FixedDictUtils.isFixedDictType(oldDictType), CommonErrorCode.FIXED_DICT::exception);
			// 修改字典数据类型，更新缓存
			new LambdaUpdateChainWrapper<>(this.dictDataMapper).set(SysDictData::getDictType, dbDict.getDictType())
					.eq(SysDictData::getDictType, oldDictType).update();
			this.deleteCache(dict.getDictType());
		}
	}

	@Override
	public boolean checkDictTypeUnique(SysDictType dict) {
		long result = this.lambdaQuery().eq(SysDictType::getDictType, dict.getDictType())
				.ne(IdUtils.validate(dict.getDictId()), SysDictType::getDictId, dict.getDictId()).count();
		return result == 0;
	}

	@Override
	public void resetDictCache() {
		clearDictCache();
		loadingDictCache();
	}

	@Override
	public void loadingDictCache() {
		List<SysDictData> list = this.dictDataMapper.selectList(null);
		list.stream().sorted(Comparator.comparing(SysDictData::getDictSort))
				.collect(Collectors.groupingBy(SysDictData::getDictType)).forEach(this::setCache);
	}

	@Override
	public void clearDictCache() {
		this.redisCache.deleteObject(this.redisCache.keys(SysConstants.CACHE_SYS_DICT_KEY + "*"));
	}

	private void deleteCache(String dictType) {
		this.redisCache.deleteObject(SysConstants.CACHE_SYS_DICT_KEY + dictType);
	}

	private void setCache(String dictType, List<SysDictData> list) {
		this.redisCache.setCacheObject(SysConstants.CACHE_SYS_DICT_KEY + dictType, list);
	}

	/**
	 * 将列表指定字段getter值通过字典数据映射字典名称到指定字段setter
	 * 
	 * @param <T>
	 * @param dictType
	 * @param list
	 * @param getter
	 * @param setter
	 */
	@Override
	public <T> void decode(String dictType, List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		Map<String, String> map = this.selectDictDatasByType(dictType).stream()
				.collect(Collectors.toMap(SysDictData::getDictValue, SysDictData::getDictLabel));
		list.forEach(dt -> {
			String value = getter.apply(dt);
			if (map.containsKey(value)) {
				setter.accept(dt, I18nUtils.get("DICT." + dictType + "." + value));
			} else {
				setter.accept(dt, value);
			}
		});
	}

	/**
	 * 初始化固定字典数据到数据库
	 */
	@Override
	public void run(String... args) throws Exception {
		this.resetDictCache();
		// 初始化固定字典数据
		FixedDictUtils.allFixedDicts().forEach(dict -> {
			List<SysDictData> dictDatas = this.selectDictDatasByType(dict.getDictType());
			if (dictDatas.isEmpty()) {
				// 无字典数据，先添加字典类型
				SysDictType dictType = this.lambdaQuery().eq(SysDictType::getDictType, dict.getDictType()).one();
				if (Objects.isNull(dictType)) {
					SysDictType dt = new SysDictType();
					dt.setDictType(dict.getDictType());
					dt.setDictName(I18nUtils.get(dict.getDictName()));
					dt.setRemark(dict.getRemark());
					dt.createBy(SysConstants.SYS_OPERATOR);
					this.save(dt);
				}
			}
			// 添加未存储的字典数据
			dict.getDataList().forEach(d -> {
				boolean contains = dictDatas.stream()
						.anyMatch(d2 -> StringUtils.equals(d2.getDictValue(), d.getValue()));
				if (!contains) {
					SysDictData data = new SysDictData();
					data.setDictType(dict.getDictType());
					data.setDictValue(d.getValue());
					data.setDictLabel(I18nUtils.get(d.getLabel()));
					data.setDictSort(d.getSort());
					data.setRemark(d.getRemark());
					data.createBy(SysConstants.SYS_OPERATOR);
					this.dictDataMapper.insert(data);
				}
			});
			this.deleteCache(dict.getDictType());
		});
	}
}
