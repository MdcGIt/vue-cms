package com.ruoyi.xmodel.service;

import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.xmodel.domain.XModel;
import com.ruoyi.xmodel.domain.XModelData;

public interface IModelDataService extends IService<XModelData> {

	/**
	 * 保存模型数据
	 * 
	 * @param modelId
	 * @param pkValue
	 * @param params
	 * @return
	 */
	void saveModelData(Long modelId, String pkValue, Map<String, Object> params);

	Map<String, Object> getModelData(XModel xmodel, String pkValue);

}
