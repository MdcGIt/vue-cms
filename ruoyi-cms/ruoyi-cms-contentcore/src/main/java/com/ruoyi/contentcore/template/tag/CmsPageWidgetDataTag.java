package com.ruoyi.contentcore.template.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.StaticizeConstants;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.staticize.tag.AbstractTag;
import com.ruoyi.common.staticize.tag.TagAttr;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IPageWidgetType;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.service.IPageWidgetService;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CmsPageWidgetDataTag extends AbstractTag {

	public final static String TAG_NAME = "cms_pagewidget_data";
	
	public final static String NAME = "页面部件数据标签";
	
	final static String TagAttr_Code = "code";
	
	private final IPageWidgetService pageWidgetService;

	@Override
	public String getTagName() {
		return TAG_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(TagAttr_Code, true, TagAttrDataType.STRING, "页面部件编码"));
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		String code = attrs.get(TagAttr_Code);
		if (StringUtils.isEmpty(code)) {
			throw new TemplateException("参数[code]不能为空", env);
		}
		Long siteId = FreeMarkerUtils.evalLongVariable(env, "Site.siteId");
		
		LambdaQueryWrapper<CmsPageWidget> q = new LambdaQueryWrapper<CmsPageWidget>()
				.eq(CmsPageWidget::getSiteId, siteId)
				.eq(CmsPageWidget::getCode, code);
		CmsPageWidget pageWidget = this.pageWidgetService.getOne(q);
		Assert.notNull(pageWidget, () -> new TemplateException(StringUtils.messageFormat("Tag attr[code={0}] data not found.", code), env));

		IPageWidgetType pwt = this.pageWidgetService.getPageWidgetType(pageWidget.getType());
		Assert.notNull(pwt, () -> new TemplateException(StringUtils.messageFormat("Unknow page widget type：{0}", pageWidget.getType()), env));
		
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		Object contentObj = pwt.parseContent(pageWidget, context.getPublishPipeCode(), context.isPreview());
		pageWidget.setContentObj(contentObj);
		return Map.of(StaticizeConstants.TemplateVariable_Data, this.wrap(env, pageWidget));
	}
	
	@Override
	public String getDescription() {
		return "页面部件数据标签，标签体内可使用${Data.name}获取数据";
	}
}
