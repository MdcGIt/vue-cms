package com.ruoyi.customform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.customform.domain.CmsCustomForm;
import com.ruoyi.customform.domain.dto.CustomFormAddDTO;
import com.ruoyi.customform.domain.dto.CustomFormEditDTO;
import com.ruoyi.customform.fixed.dict.CustomFormStatus;
import com.ruoyi.customform.mapper.CustomFormMapper;
import com.ruoyi.customform.service.ICustomFormService;
import com.ruoyi.xmodel.XModelUtils;
import com.ruoyi.xmodel.core.impl.MetaControlType_Input;
import com.ruoyi.xmodel.db.DbTable;
import com.ruoyi.xmodel.db.DbTableColumn;
import com.ruoyi.xmodel.domain.XModel;
import com.ruoyi.xmodel.domain.XModelData;
import com.ruoyi.xmodel.domain.XModelField;
import com.ruoyi.xmodel.dto.XModelDTO;
import com.ruoyi.xmodel.exception.MetaErrorCode;
import com.ruoyi.xmodel.mapper.XModelDataMapper;
import com.ruoyi.xmodel.mapper.XModelFieldMapper;
import com.ruoyi.xmodel.mapper.XModelMapper;
import com.ruoyi.xmodel.service.IModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomFormServiceImpl extends ServiceImpl<CustomFormMapper, CmsCustomForm> implements ICustomFormService {

	public final static String OwnerType_CustomForm = "custom_form";

	private final XModelMapper modelMapper;

	private final IModelService modelService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addCustomForm(CustomFormAddDTO dto) {
		CmsCustomForm customForm = new CmsCustomForm();
		customForm.setFormId(IdUtils.getSnowflakeId());
		customForm.setSiteId(dto.getSiteId());
		customForm.setModelId(customForm.getFormId());
		customForm.setStatus(0);
		customForm.setName(dto.getName());
		customForm.setCode(dto.getCode());
		customForm.setRemark(dto.getRemark());
		customForm.createBy(dto.getOperator().getUsername());
		this.save(customForm);
		// 创建关联元数据模型
		XModel xModel = new XModel();
		xModel.setModelId(customForm.getFormId());
		xModel.setName(customForm.getName());
		xModel.setCode(customForm.getFormId().toString());
		xModel.setTableName(dto.getTableName());
		xModel.setOwnerType(OwnerType_CustomForm);
		xModel.setOwnerId(customForm.getSiteId().toString());
		xModel.createBy(dto.getOperator().getUsername());
		modelMapper.insert(xModel);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void editCustomForm(CustomFormEditDTO dto) {
		CmsCustomForm form = this.getById(dto.getFormId());
		Assert.notNull(form, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("formId", dto.getFormId()));

		if (!dto.getName().equals(form.getName())) {
			// 更新元数据模型名称
			XModel model = this.modelMapper.selectById(dto.getFormId());
			model.setName(form.getName());
			this.modelService.lambdaUpdate().set(XModel::getName, form.getName())
					.eq(XModel::getModelId, form.getFormId()).update();
		}
		form.setName(dto.getName());
		form.setCode(dto.getCode());
		form.setRemark(dto.getRemark());
		dto.getTemplates().forEach(item -> {
			form.getTemplates().put(item.get("code"), item.get("template"));
		});
		this.updateById(form);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteCustomForm(List<Long> formIds) {
		// 删除自定义表单数据
		this.removeByIds(formIds);
		// 删除元数据模型及数据
		this.modelService.deleteModel(formIds);
	}

	@Override
	public void publishCustomForms(List<Long> formIds, String username) {
		// 更改状态
		List<CmsCustomForm> forms = this.listByIds(formIds);
		for (CmsCustomForm form : forms) {
			if (form.getStatus() != CustomFormStatus.PUBLISHED) {
				form.setStatus(CustomFormStatus.PUBLISHED);
				this.updateById(form);
			}
		}
		// 生成静态页面
		for (CmsCustomForm form : forms) {
			form.getTemplates().entrySet().forEach(e -> {
				String publishPipeCode = e.getKey();
				String template = e.getValue();
				if (StringUtils.isNotEmpty(template)) {
					this.customFormStaticize(form, publishPipeCode, template);
				}
			});
		}
	}

	/**
	 * TODO: 静态化
	 */
	private void customFormStaticize(CmsCustomForm form, String publishPipeCode, String template) {

	}
}
