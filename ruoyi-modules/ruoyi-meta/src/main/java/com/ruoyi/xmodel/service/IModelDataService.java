package com.ruoyi.xmodel.service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.common.mybatisplus.util.SqlBuilder;
import com.ruoyi.xmodel.domain.XModelData;

public interface IModelDataService extends IService<XModelData> {

	/**
	 * 根据主键获取模型数据
	 *
	 * @param modelId
	 * @param pkValues
	 * @return
	 */
	Map<String, Object> getModelDataByPkValue(Long modelId, Map<String, Object> pkValues);

	/**
	 * 保存模型数据
	 * 
	 * @param modelId
	 * @param params
	 * @return
	 */
	void saveModelData(Long modelId, Map<String, Object> params);

	/**
	 * 添加元数据模型数据
	 *
	 * @param modelId
	 * @param data
	 */
    void addModelData(Long modelId, Map<String, Object> data);

	/**
	 * 更新元数据模型数据
	 *
	 * @param modelId
	 * @param data
	 */
	void updateModelData(Long modelId, Map<String, Object> data);

	/**
	 * 删除元数据模型数据
	 *
	 * @param modeId
	 * @param pkValues
	 */
	void deleteModelDataByPkValue(Long modeId, List<Map<String, String>> pkValues);

    List<Map<String, Object>> selectModelDataList(Long modelId, Consumer<SqlBuilder> consumer);

	IPage<Map<String, Object>> selectModelDataPage(Long modelId, IPage<Map<String, Object>> page,
												   Consumer<SqlBuilder> consumer);
}
