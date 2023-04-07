package com.ruoyi.exmodel.template.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.staticize.tag.AbstractTag;
import com.ruoyi.common.staticize.tag.TagAttr;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.xmodel.domain.XModel;
import com.ruoyi.xmodel.service.IModelDataService;
import com.ruoyi.xmodel.service.IModelService;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CmsXModelDataTag extends AbstractTag {

	public final static String TAG_NAME = "cms_xmodel_data";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	public final static String TagAttr_ModelId = "modelid";

	public final static String TagAttr_PkValue = "pkvalue";

	public final static String TemplateVariable_ModelData = "ModelData";
	
	private final IModelService modelService;
	
	private final IModelDataService modelDataService;
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(TagAttr_ModelId, true, TagAttrDataType.INTEGER, "模型ID") );
		tagAttrs.add(new TagAttr(TagAttr_PkValue, true, TagAttrDataType.STRING, "模型数据ID") );
		return tagAttrs;
	}
	
	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		long modelId = MapUtils.getLongValue(attrs, TagAttr_ModelId, 0);
		if (modelId <= 0) {
			throw new TemplateException("扩展模型数据ID错误：" + modelId, env);
		}
		String pkValue = MapUtils.getString(attrs, TagAttr_PkValue);
		if (StringUtils.isEmpty(pkValue)) {
			throw new TemplateException("扩展模型数据ID不能为空：" + pkValue, env);
		}
		XModel xmodel = this.modelService.getById(modelId);
		if (xmodel == null) {
			throw new TemplateException("扩展模型数据未找到：" + modelId, env);
		}
		Map<String, Object> modelData = this.modelDataService.getModelData(xmodel, pkValue);
		return Map.of(TemplateVariable_ModelData, this.wrap(env, modelData));
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
