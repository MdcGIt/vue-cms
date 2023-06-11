package com.ruoyi.customform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.customform.domain.CmsCustomForm;
import com.ruoyi.customform.domain.dto.CustomFormAddDTO;
import com.ruoyi.customform.domain.dto.CustomFormEditDTO;
import com.ruoyi.xmodel.domain.XModel;
import com.ruoyi.xmodel.dto.XModelDTO;

import java.util.List;

public interface ICustomFormService extends IService<CmsCustomForm> {

	/**
	 * 添加自定义表单
	 * 
	 * @param dto
	 * @return
	 */
	void addCustomForm(CustomFormAddDTO dto);
	
	/**
	 * 编辑自定义表单
	 * 
	 * @param dto
	 * @return
	 */
	void editCustomForm(CustomFormEditDTO dto);
	
	/**
	 * 删除自定义表单
	 * 
	 * @param formIds
	 * @return
	 */
	void deleteCustomForm(List<Long> formIds);

	/**
	 * 发布自定义表单
	 *
	 * @param formIds
	 * @param username
	 */
    void publishCustomForms(List<Long> formIds, String username);
}
