package com.ruoyi.xmodel.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.xmodel.domain.XModelField;
import com.ruoyi.xmodel.dto.XModelFieldDTO;
import com.ruoyi.xmodel.dto.XModelFieldDataDTO;

public interface IModelFieldService extends IService<XModelField> {

	/**
	 * 添加元数据模型字段
	 * 
	 * @param dto
	 */
	void addModelField(XModelFieldDTO dto);

	/**
	 * 更新元数据模型字段
	 * 
	 * @param dto
	 */
	void editModelField(XModelFieldDTO dto);

	/**
	 * 删除元数据模型字段
	 * 
	 * @param fieldIds
	 */
	void deleteModelField(List<Long> fieldIds);

	/**
	 * 获取元数据模型数据
	 * 
	 * @param modelId
	 * @param pkValue
	 * @return
	 */
	List<XModelFieldDataDTO> getFieldDatas(Long modelId, String pkValue);
}
