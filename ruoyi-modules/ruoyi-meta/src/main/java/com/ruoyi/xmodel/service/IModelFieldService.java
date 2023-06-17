package com.ruoyi.xmodel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.xmodel.domain.XModelField;
import com.ruoyi.xmodel.dto.XModelFieldDTO;

import java.util.List;

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
}
