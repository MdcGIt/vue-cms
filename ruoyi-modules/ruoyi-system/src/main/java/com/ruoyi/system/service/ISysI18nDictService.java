package com.ruoyi.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.SysI18nDict;

public interface ISysI18nDictService extends IService<SysI18nDict> {

	/**
	 * 获取国际化键值
	 * 
	 * @param languageTag
	 * @param langKey
	 * @return
	 */
	public String getLangValue(String languageTag, String langKey);

	/**
	 * 获取指定国际化键名的数据列表
	 * 
	 * @param langKey
	 * @return
	 */
	List<SysI18nDict> listByLangKey(String langKey);

	/**
	 * 新增国际化字典数据
	 * 
	 * @param dict
	 */
	void insertI18nDict(SysI18nDict dict);

	/**
	 * 更新国际化字典数据
	 * 
	 * @param dict
	 */
	void updateI18nDict(SysI18nDict dict);
	
	/**
	 * 批量更新国际化字典数据，不存在则插入
	 * 
	 * @param dicts
	 */
	void batchSaveI18nDicts(List<SysI18nDict> dicts);

	/**
	 * 删除国际化字典数据
	 * 
	 * @param i18nDictIds
	 */
	public void deleteI18nDictByIds(List<Long> i18nDictIds);

	/**
	 * 刷新缓存
	 */
	public void resetCache();
}
