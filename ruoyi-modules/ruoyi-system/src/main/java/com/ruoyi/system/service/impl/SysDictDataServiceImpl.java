package com.ruoyi.system.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.SysConstants;
import com.ruoyi.system.domain.SysDictData;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.fixed.FixedDictUtils;
import com.ruoyi.system.mapper.SysDictDataMapper;
import com.ruoyi.system.service.ISysDictDataService;

import lombok.RequiredArgsConstructor;

/**
 * 字典 业务层处理
 * 
 * @author ruoyi
 */
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements ISysDictDataService {

	private final RedisCache redisCache;
	
	/**
	 * 批量删除字典数据信息
	 */
	@Override
	public void deleteDictDataByIds(List<Long> dictDataIds) {
		List<SysDictData> list = this.listByIds(dictDataIds);
		list.forEach(data -> {
			Assert.isFalse(isFixed(data), CommonErrorCode.FIXED_DICT::exception);
		});
		this.removeBatchByIds(list);
		// 删除缓存
		list.stream().map(SysDictData::getDictType).distinct().forEach(dictType -> {
			this.redisCache.deleteObject(SysConstants.CACHE_SYS_DICT_KEY + dictType);
		});
	}

	/**
	 * 是否系统固定字典数据
	 * 
	 * @param data
	 * @return
	 */
	private boolean isFixed(SysDictData data) {
		FixedDictType dictType = FixedDictUtils.getFixedDictType(data.getDictType());
		return dictType.getDataList().stream().anyMatch(d -> d.getValue().equals(data.getDictValue()));
	}

	/**
	 * 新增保存字典数据信息
	 * 
	 * @param data 字典数据信息
	 * @return 结果
	 */
	@Override
	public void insertDictData(SysDictData data) {
		Assert.isTrue(this.checkDictValueUnique(data),
				() -> CommonErrorCode.DATA_CONFLICT.exception("dictValue:" + data.getDictType()));

		FixedDictType dictType = FixedDictUtils.getFixedDictType(data.getDictType());
		if (Objects.nonNull(dictType) && !dictType.isAllowAdd()) {
			throw CommonErrorCode.FIXED_DICT_NOT_ALLOW_ADD.exception();
		}
		
		data.setCreateTime(LocalDateTime.now());
		if (this.save(data)) {
			this.redisCache.deleteObject(SysConstants.CACHE_SYS_DICT_KEY + data.getDictType());
		}
	}

	/**
	 * 修改保存字典数据信息
	 * 
	 * @param data 字典数据信息
	 * @return 结果
	 */
	@Override
	public void updateDictData(SysDictData data) {
		SysDictData dbData = this.getById(data.getDictCode());
		// 是否存在
		Assert.notNull(dbData, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("dictCode", data.getDictCode()));
		// 是否系统固定字典数据，固定数据不允许修改字典数据值
		Assert.isFalse(isFixed(dbData) && !StringUtils.equals(dbData.getDictValue(), data.getDictValue()),
				CommonErrorCode.FIXED_DICT::exception);
		// 字典数据值是否冲突
		Assert.isTrue(this.checkDictValueUnique(data),
				() -> CommonErrorCode.DATA_CONFLICT.exception("dictValue:" + data.getDictValue()));

		dbData.setDictValue(data.getDictValue());
		dbData.setDictLabel(data.getDictLabel());
		dbData.setDictSort(data.getDictSort());
		dbData.setIsDefault(data.getIsDefault());
		dbData.setCssClass(data.getCssClass());
		dbData.setListClass(data.getListClass());
		dbData.setRemark(data.getRemark());
		dbData.setUpdateTime(LocalDateTime.now());

		if (this.updateById(dbData)) {
			this.redisCache.deleteObject(SysConstants.CACHE_SYS_DICT_KEY + data.getDictType());
		}
	}

	/**
	 * 校验字典数据项值是否冲突
	 * 
	 * @param data
	 * @return
	 */
	public boolean checkDictValueUnique(SysDictData data) {
		long result = this.count(new LambdaQueryWrapper<SysDictData>()
				.eq(SysDictData::getDictValue, data.getDictValue()).eq(SysDictData::getDictType, data.getDictType())
				.ne(IdUtils.validate(data.getDictCode()), SysDictData::getDictCode, data.getDictCode()));
		return result == 0;
	}
}
