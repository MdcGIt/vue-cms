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
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.fixed.config.TemplateSuffix;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.service.ITemplateService;
import com.ruoyi.contentcore.util.SiteUtils;

import freemarker.core.Environment;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class CmsIncludeTag extends AbstractTag {

	public static final String TAG_NAME = "cms_include";
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

	private static final String TagAttr_FILE = "file";

	private static final String TagAttr_SSI = "ssi";

	private static final String TagAttr_VIRTUAL = "virtual";

	/**
	 * <@cms_include file="footer.template.html"></@cms_include>
	 */
	public static final String SSI_INCLUDE_TAG = "<!--#include file=\"{0}\" -->\n";

	/**
	 * 使用virtual来访问动态区块内容，实际访问后台/ssi/virtual接口返回html内容后包含在前端页面中。
	 * 与ajax异步获取内容不同，此方式可将动态内容与html同步返回有利于Spider获取页面更新， 包含模板中可用${Request.xxx}获取参数值
	 * 
	 * <@cms_include virtual="footer.template.html?t=123&c=ddd"></@cms_include>
	 */
	public static final String SSI_INCLUDE_VIRTUAL_TAG = "<!--#include virtual=\"{0}\" -->\n";

	private final ISiteService siteService;

	private final ITemplateService templateService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(TagAttr_FILE, true, TagAttrDataType.STRING, "引用模板文件路径（相对模板目录template/）"));
		tagAttrs.add(new TagAttr(TagAttr_SSI, false, TagAttrDataType.BOOLEAN, "是否启用SSI", "true"));
		tagAttrs.add(new TagAttr(TagAttr_VIRTUAL, false, TagAttrDataType.BOOLEAN, "是否启用virtual，此模式下区块无法继承当前页面上限文变量，需要通过参数传入需要的变量", "false"));
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);

		String file = attrs.get(TagAttr_FILE);
		Assert.notEmpty(file, () -> new TemplateException("参数[file]不能为空", env));
		boolean ssi = Boolean.valueOf(attrs.get(TagAttr_SSI));
		boolean virtual = Boolean.valueOf(attrs.get(TagAttr_VIRTUAL));

		long siteId = FreeMarkerUtils.evalLongVariable(env, "Site.siteId");
		CmsSite site = this.siteService.getSite(siteId);

		String includeTemplateName = SiteUtils.getTemplateName(site, context.getPublishPipeCode(), file);
		if (context.isPreview()) {
			Template includeTemplate = env.getTemplateForInclusion(includeTemplateName,
					StandardCharsets.UTF_8.displayName(), true);
			env.include(includeTemplate);
		} else if (virtual) {
			String t = file;
			String params = null;
			if (file.indexOf("?") > -1) {
				String[] split = StringUtils.split(file, "?");
				t = split[0];
				params = split[1];
			}
			// 动态模板
			String virtualPath = "/cms/ssi/virtual?sid=" + siteId + "&pp=" + context.getPublishPipeCode() + "&t=" + t + "&" + params;
			env.getOut().write(StringUtils.messageFormat(SSI_INCLUDE_VIRTUAL_TAG, virtualPath));
		} else {
			String siteRoot = SiteUtils.getSiteRoot(site, context.getPublishPipeCode());
			String staticFilePath = siteRoot + getStaticPath(site, context.getPublishPipeCode(), includeTemplateName);
			String staticContent = templateService.getTemplateStaticContentCache(includeTemplateName);
			if (Objects.isNull(staticContent) || !new File(staticFilePath).exists()) {
				staticContent = this.writeTo(env, context, includeTemplateName, staticFilePath);
			}
			if (ssi) {
				env.getOut().write(StringUtils.messageFormat(SSI_INCLUDE_TAG, "/" + staticFilePath));
			} else {
				env.getOut().write(staticContent);
			}
		}
		return null;
	}

	/**
	 * 静态文件相对路径
	 */
	private String getStaticPath(CmsSite site, String publishPipeCode, String includeTemplateName) {
		String siteTemplatePath = SiteUtils.getSiteTemplatePath(site.getPath(), publishPipeCode);
		return "include/"
				+ includeTemplateName.substring(siteTemplatePath.length(),
						includeTemplateName.length() - TemplateSuffix.getValue().length())
				+ "." + site.getStaticSuffix(publishPipeCode);
	}

	/**
	 * 静态化内容写入文件
	 */
	private String writeTo(Environment env, TemplateContext context, String includeTemplateName, String staticFilePath)
			throws TemplateException, IOException {
		String staticContent = processTemplate(env, context, includeTemplateName);
		FileUtils.writeStringToFile(new File(staticFilePath), staticContent, StandardCharsets.UTF_8);
		this.templateService.setTemplateStaticContentCache(includeTemplateName, staticContent);
		return staticContent;
	}

	/**
	 * 生成包含模板静态化内容
	 */
	private String processTemplate(Environment env, TemplateContext context, String includeTemplateName)
			throws TemplateException, IOException {
		Writer out = env.getOut();
		try (StringWriter writer = new StringWriter()) {
			env.setOut(writer);
			Template includeTemplate = env.getTemplateForInclusion(includeTemplateName,
					StandardCharsets.UTF_8.displayName(), true);
			env.include(includeTemplate);
			return writer.getBuffer().toString();
		} finally {
			env.setOut(out);
		}
	}
}
