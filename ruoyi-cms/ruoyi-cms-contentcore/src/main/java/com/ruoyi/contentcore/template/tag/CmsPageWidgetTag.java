package com.ruoyi.contentcore.template.tag;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.staticize.tag.AbstractTag;
import com.ruoyi.common.staticize.tag.TagAttr;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IPageWidgetType;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.IPageWidgetService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.service.ITemplateService;
import com.ruoyi.contentcore.util.PageWidgetUtils;
import com.ruoyi.contentcore.util.SiteUtils;

import freemarker.core.Environment;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CmsPageWidgetTag extends AbstractTag {

	public final static String TAG_NAME = "cms_pagewidget";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

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

	final static String TagAttr_Code = "code";

	final static String TagAttr_SSI = "ssi";
	
	private final ISiteService siteService;

	private final IPageWidgetService pageWidgetService;

	private final ITemplateService templateService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(TagAttr_Code, true, TagAttrDataType.STRING, "页面部件编码"));
		tagAttrs.add(new TagAttr(TagAttr_SSI, false, TagAttrDataType.BOOLEAN, "是否启用SSI", "true"));
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);

		String code = attrs.get(TagAttr_Code);
		Assert.notEmpty(code, () -> new TemplateException("参数[code]不能为空", env));
		boolean ssi = Boolean.valueOf(attrs.get(TagAttr_SSI));

		long siteId = FreeMarkerUtils.evalLongVariable(env, "Site.siteId");
		CmsPageWidget pw = this.pageWidgetService.lambdaQuery().eq(CmsPageWidget::getSiteId, siteId)
				.eq(CmsPageWidget::getCode, code).one();
		Assert.notNull(pw, () -> new TemplateException(StringUtils.messageFormat("页面部件[{0}]不存在", code), env));

		IPageWidgetType pwt = this.pageWidgetService.getPageWidgetType(pw.getType());
		Assert.notNull(pwt, () -> new TemplateException(StringUtils.messageFormat("页面部件类型错误：{0}", pw.getType()), env));
		
		CmsSite site = this.siteService.getSite(siteId);
		File templateFile = this.templateService.findTemplateFile(site, pw.getTemplate(), context.getPublishPipeCode());
		Assert.notNull(templateFile, () -> new TemplateException(StringUtils.messageFormat("页面部件[{0}]指定模板[{1}]不存在", code, pw.getTemplate()), env));

		String templateName = SiteUtils.getTemplateName(site, pw.getPublishPipeCode(), pw.getTemplate());
		if (context.isPreview()) {
			env.getOut().write(this.processTemplate(env, context, templateName));
		} else {
			String siteRoot = SiteUtils.getSiteRoot(site, context.getPublishPipeCode());
			String staticFileName = PageWidgetUtils.getStaticFileName(pw, site.getStaticSuffix(pw.getPublishPipeCode()));
			String staticFilePath = siteRoot + pw.getPath() + staticFileName;
			// 读取页面部件静态化内容
			String staticContent = templateService.getTemplateStaticContentCache(templateName);
			if (Objects.isNull(staticContent) || !new File(staticFilePath).exists()) {
				staticContent = this.processTemplate(env, context, templateName);
				FileUtils.writeStringToFile(new File(siteRoot + staticFilePath), staticContent, StandardCharsets.UTF_8);
				this.templateService.setTemplateStaticContentCache(templateName, staticContent);
			}
			if (ssi) {
				env.getOut().write(StringUtils.messageFormat(CmsIncludeTag.SSI_INCLUDE_TAG, "/" + staticFilePath));
			} else {
				env.getOut().write(staticContent);
			}
		}
		return null;
	}
	
	private String processTemplate(Environment env, TemplateContext context, String templateName)
			throws TemplateException, IOException {
		Writer out = env.getOut();
		try (StringWriter writer = new StringWriter()) {
			env.setOut(writer);
			Template includeTemplate = env.getTemplateForInclusion(templateName,
					StandardCharsets.UTF_8.displayName(), true);
			env.include(includeTemplate);
			return writer.getBuffer().toString();
		} finally {
			env.setOut(out);
		}
	}
}
