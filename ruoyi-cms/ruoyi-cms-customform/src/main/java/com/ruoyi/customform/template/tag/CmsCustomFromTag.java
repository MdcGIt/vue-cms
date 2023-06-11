package com.ruoyi.customform.template.tag;

import com.ruoyi.common.staticize.StaticizeConstants;
import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.staticize.tag.AbstractTag;
import com.ruoyi.common.staticize.tag.TagAttr;
import com.ruoyi.common.utils.ReflectASMUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.fixed.config.BackendContext;
import com.ruoyi.contentcore.fixed.config.SiteApiUrl;
import com.ruoyi.contentcore.util.TemplateUtils;
import com.ruoyi.customform.domain.CmsCustomForm;
import com.ruoyi.customform.service.ICustomFormService;
import com.ruoyi.xmodel.domain.XModel;
import com.ruoyi.xmodel.service.IModelDataService;
import com.ruoyi.xmodel.service.IModelService;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class CmsCustomFromTag extends AbstractTag {

	public final static String TAG_NAME = "cms_custom_form";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	public final static String TagAttr_Code = "code";

	private final ICustomFormService customFormService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(TagAttr_Code, true, TagAttrDataType.STRING, "自定义表单编码") );
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		String code = attrs.get(TagAttr_Code);
		Optional<CmsCustomForm> opt = this.customFormService.lambdaQuery()
				.eq(CmsCustomForm::getCode, code).oneOpt();
		if (opt.isEmpty()) {
			throw new TemplateException("自定义表单未找到：" + code, env);
		}
		CmsCustomForm customForm = opt.get();
		Map<String, Object> data = ReflectASMUtils.beanToMap(customForm);
		data.put("action", SiteApiUrl.getValue() + "api/customform/submit");
		return Map.of(StaticizeConstants.TemplateVariable_Data, this.wrap(env, data));
	}

	@Override
	public String getTagName() {
		return TAG_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return DESC;
	}
}
