package com.ruoyi.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.system.domain.SysConfig;

/**
 * 参数配置 服务层
 * 
 * @author ruoyi
 */
public interface ISysConfigService extends IService<SysConfig> {

	/**
	 * 根据键名查询参数配置信息
	 * 
	 * @param configKey
	 *            参数键名
	 * @return 参数键值
	 */
	public String selectConfigByKey(String configKey);

	/**
	 * 新增参数配置
	 * 
	 * @param config
	 *            参数配置信息
	 * @return 结果
	 */
	public void insertConfig(SysConfig config);

	/**
	 * 修改参数配置
	 * 
	 * @param config
	 *            参数配置信息
	 * @return 结果
	 */
	public void updateConfig(SysConfig config);

	/**
	 * 批量删除参数信息
	 * 
	 * @param configIds
	 *            需要删除的参数ID
	 */
	public void deleteConfigByIds(List<Long> configIds);

	/**
	 * 加载参数缓存数据
	 */
	public void loadingConfigCache();

	/**
	 * 清空参数缓存数据
	 */
	public void clearConfigCache();

	/**
	 * 重置参数缓存数据
	 */
	public void resetConfigCache();
}
