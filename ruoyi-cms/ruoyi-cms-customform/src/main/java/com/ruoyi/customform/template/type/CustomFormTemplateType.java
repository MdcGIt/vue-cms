package com.ruoyi.customform.template.type;

import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.ReflectASMUtils;
import com.ruoyi.contentcore.fixed.config.SiteApiUrl;
import com.ruoyi.contentcore.template.ITemplateType;
import com.ruoyi.customform.domain.CmsCustomForm;
import com.ruoyi.customform.service.ICustomFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor
@Component(ITemplateType.BEAN_NAME_PREFIX + CustomFormTemplateType.TypeId)
public class CustomFormTemplateType implements ITemplateType {
	
	public final static String TypeId = "CustomForm";

	public static final String STATICIZE_DIRECTORY = "include/customform/";

	public static final String TemplateVariable_CustomForm = "CustomForm";

	private final ICustomFormService customFormService;

	@Override
	public String getId() {
		return TypeId;
	}

	@Override
	public void initTemplateData(Object dataId, TemplateContext context) {
		CmsCustomForm form = this.customFormService.getById(dataId.toString());

		context.getVariables().put(TemplateVariable_CustomForm, getCustomFormVariables(form));
	}

	public static Map<String, Object> getCustomFormVariables(CmsCustomForm form) {
		Map<String, Object> map = ReflectASMUtils.beanToMap(form);
		map.put("action", SiteApiUrl.getValue() + "api/customform/submit");
		return map;
	}
}
