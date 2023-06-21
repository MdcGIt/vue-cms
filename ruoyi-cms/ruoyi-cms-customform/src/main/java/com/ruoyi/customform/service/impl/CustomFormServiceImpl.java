package com.ruoyi.customform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.staticize.StaticizeService;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ReflectASMUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsPublishPipe;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.fixed.config.SiteApiUrl;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.service.ITemplateService;
import com.ruoyi.contentcore.template.ITemplateType;
import com.ruoyi.contentcore.template.impl.SiteTemplateType;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.contentcore.util.TemplateUtils;
import com.ruoyi.customform.CmsCustomFormMetaModelType;
import com.ruoyi.customform.CustomFormConsts;
import com.ruoyi.customform.domain.CmsCustomForm;
import com.ruoyi.customform.domain.dto.CustomFormAddDTO;
import com.ruoyi.customform.domain.dto.CustomFormEditDTO;
import com.ruoyi.customform.fixed.dict.CustomFormStatus;
import com.ruoyi.customform.mapper.CustomFormMapper;
import com.ruoyi.customform.publishpipe.PublishPipeProp_CustomFormTemplate;
import com.ruoyi.customform.service.ICustomFormService;
import com.ruoyi.xmodel.domain.XModel;
import com.ruoyi.xmodel.service.IModelService;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomFormServiceImpl extends ServiceImpl<CustomFormMapper, CmsCustomForm> implements ICustomFormService {

	private static final Logger logger = LoggerFactory.getLogger(CustomFormServiceImpl.class);

	private final IModelService modelService;

	private final ISiteService siteService;

	private final ITemplateService templateService;

	private final IPublishPipeService publishPipeService;

	private final StaticizeService staticizeService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void addCustomForm(CustomFormAddDTO dto) {
		CmsCustomForm customForm = new CmsCustomForm();
		customForm.setFormId(IdUtils.getSnowflakeId());
		customForm.setSiteId(dto.getSiteId());
		customForm.setModelId(customForm.getFormId());
		customForm.setName(dto.getName());
		customForm.setCode(dto.getCode());
		customForm.setStatus(CustomFormStatus.DRAFT);
		customForm.setNeedCaptcha(dto.getNeedCaptcha());
		customForm.setNeedLogin(dto.getNeedLogin());
		customForm.setRuleLimit(dto.getRuleLimit());
		customForm.setRemark(dto.getRemark());
		customForm.createBy(dto.getOperator().getUsername());
		this.save(customForm);
		// 创建关联元数据模型
		XModel xModel = new XModel();
		xModel.setModelId(customForm.getFormId());
		xModel.setName(customForm.getName());
		xModel.setCode(customForm.getFormId().toString());
		xModel.setTableName(dto.getTableName());
		xModel.setOwnerType(CmsCustomFormMetaModelType.TYPE);
		xModel.createBy(dto.getOperator().getUsername());
		this.modelService.save(xModel);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void editCustomForm(CustomFormEditDTO dto) {
		CmsCustomForm form = this.getById(dto.getFormId());
		Assert.notNull(form, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("formId", dto.getFormId()));

		if (!dto.getName().equals(form.getName())) {
			// 更新元数据模型名称
			XModel model = this.modelService.getById(dto.getFormId());
			model.setName(form.getName());
			this.modelService.lambdaUpdate().set(XModel::getName, form.getName())
					.eq(XModel::getModelId, form.getFormId()).update();
			this.modelService.clearMetaModelCache(model.getModelId());
		}
		form.setName(dto.getName());
		form.setCode(dto.getCode());
		form.setNeedCaptcha(dto.getNeedCaptcha());
		form.setNeedLogin(dto.getNeedLogin());
		form.setRuleLimit(dto.getRuleLimit());
		form.setRemark(dto.getRemark());
		dto.getTemplates().forEach(item -> form.getTemplates().put(item.get("code"), item.get("template")));
		this.updateById(form);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteCustomForm(List<Long> formIds) {
		// 删除自定义表单数据
		this.removeByIds(formIds);
		// 删除元数据模型及数据
		for (Long formId : formIds) {
			this.modelService.getBaseMapper().deleteById(formId);
		}
	}

	@Override
	public void offlineCustomForms(List<Long> formIds, String username) throws IOException {
		// 更改状态
		List<CmsCustomForm> forms = this.listByIds(formIds);
		if (forms.size() == 0) {
			return;
		}
		CmsSite site = this.siteService.getSite(forms.get(0).getSiteId());
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(site.getSiteId());
		for (CmsCustomForm form : forms) {
			if (form.getStatus() != CustomFormStatus.OFFLINE) {
				form.setStatus(CustomFormStatus.OFFLINE);
				this.updateById(form);
			}
			// 删除静态文件
			for (CmsPublishPipe pp : publishPipes) {
				String siteRoot = SiteUtils.getSiteRoot(site, pp.getCode());
				File f = new File(siteRoot + CustomFormConsts.STATICIZE_DIRECTORY
						+ form.getCode() + "." + site.getStaticSuffix(pp.getCode()));
				if (f.exists()) {
					FileUtils.delete(f);
				}
			}
		}
	}

	@Override
	public void publishCustomForms(List<Long> formIds, String username) {
		// 更改状态
		List<CmsCustomForm> forms = this.listByIds(formIds);
		if (forms.size() == 0) {
			return;
		}
		CmsSite site = this.siteService.getSite(forms.get(0).getSiteId());
		List<CmsPublishPipe> publishPipes = this.publishPipeService.getPublishPipes(site.getSiteId());
		for (CmsCustomForm form : forms) {
			if (form.getStatus() != CustomFormStatus.PUBLISHED) {
				form.setStatus(CustomFormStatus.PUBLISHED);
				this.updateById(form);
			}
			// 生成静态页面
			publishPipes.forEach(pp -> this.customFormStaticize(form, pp.getCode()));
		}
	}

	private void customFormStaticize(CmsCustomForm form, String publishPipeCode) {
		CmsSite site = this.siteService.getSite(form.getSiteId());

		String template = form.getTemplates().get(publishPipeCode);
		if (StringUtils.isEmpty(template)) {
			template = PublishPipeProp_CustomFormTemplate.getValue(publishPipeCode, site.getPublishPipeProps());
		}
		if (StringUtils.isEmpty(template)) {
			logger.warn("[{}]自定义表单[{}]模板未设置或文件不存在", publishPipeCode, form.getName());
			return; // 未设置模板不生成静态文件
		}
		try {
			// 模板上下文
			String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, template);
			TemplateContext templateContext = new TemplateContext(templateKey, false, publishPipeCode);
			// init template datamode
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(SiteTemplateType.TypeId);
			templateType.initTemplateData(form.getSiteId(), templateContext);
			templateContext.getVariables().put(CustomFormConsts.TemplateVariable_CustomForm,
					CustomFormConsts.getCustomFormVariables(form));
			// 静态化文件地址
			String siteRoot = SiteUtils.getSiteRoot(site, publishPipeCode);
			templateContext.setDirectory(siteRoot + CustomFormConsts.STATICIZE_DIRECTORY);
			String fileName = form.getCode() + "." + site.getStaticSuffix(publishPipeCode);
			templateContext.setFirstFileName(fileName);
			// 静态化
			this.staticizeService.process(templateContext);
		} catch (TemplateException | IOException e) {
			logger.warn(AsyncTaskManager.addErrMessage(StringUtils.messageFormat("[{0}]自定义表单模板解析失败：{1}",
					publishPipeCode, form.getName())));
			e.printStackTrace();
		}
	}
}
