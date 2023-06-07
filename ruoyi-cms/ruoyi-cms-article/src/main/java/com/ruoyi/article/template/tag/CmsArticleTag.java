package com.ruoyi.article.template.tag;

import com.ruoyi.article.domain.CmsArticleDetail;
import com.ruoyi.article.mapper.CmsArticleDetailMapper;
import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.StaticizeConstants;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.staticize.tag.AbstractTag;
import com.ruoyi.common.staticize.tag.TagAttr;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.enums.ContentCopyType;
import com.ruoyi.contentcore.mapper.CmsContentMapper;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class CmsArticleTag extends AbstractTag {

	public static final String TAG_NAME = "cms_article";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	public static final String TagAttr_ContentId = "contentId";

	public static final String TagAttr_Page = "page";

	public static final String TemplateVariable_ArticleContent = "ArticleContent";

	// CKEditor5: <div class="page-break" style="page-break-after:always;"><span style="display:none;">&nbsp;</span></div>
//	private static final String PAGE_BREAK_SPLITER = "<div[^>]+class=['\"]page-break['\"].*?</div>";
	private static final String PAGE_BREAK_SPLITER = "__XY_UEDITOR_PAGE_BREAK__";


	private final CmsContentMapper contentMapper;

	private final CmsArticleDetailMapper articleMapper;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(TagAttr_ContentId, true, TagAttrDataType.INTEGER, "文章内容ID"));
		tagAttrs.add(new TagAttr(TagAttr_Page, false, TagAttrDataType.BOOLEAN, "是否分页，默认false"));
		return tagAttrs;
	}

	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		String contentHtml = null;
		long contentId = MapUtils.getLongValue(attrs, TagAttr_ContentId, 0);
		if (contentId <= 0) {
			throw new TemplateException("Invalid contentId: " + contentId, env);
		}
		CmsContent content = this.contentMapper.selectById(contentId);
		if (content.isLinkContent()) {
			return Map.of(TemplateVariable_ArticleContent, this.wrap(env, StringUtils.EMPTY));
		}
		if (ContentCopyType.isMapping(content.getCopyType())) {
			contentId = content.getCopyId();
		}
		CmsArticleDetail articleDetail = this.articleMapper.selectById(contentId);
		if (Objects.isNull(articleDetail)) {
			throw new TemplateException("Article details not found: " + contentId, env);
		}
		contentHtml = articleDetail.getContentHtml();
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		boolean page = MapUtils.getBooleanValue(attrs, TagAttr_Page, false);
		if (page) {
			if (context.isPaged()) {
				throw new TemplateException("分页标识已被其他标签激活", env);
			}
			context.setPaged(true);

			String[] pageContents = contentHtml.split(PAGE_BREAK_SPLITER);
			if (context.getPageIndex() > pageContents.length) {
				throw new TemplateException(StringUtils.messageFormat("文章内容分页越界：{0}, 最大页码：{1}。", context.getPageIndex(),
						pageContents.length), env);
			}
			context.setPageTotal(pageContents.length);
			env.setGlobalVariable(StaticizeConstants.TemplateVariable_PageTotal,
					this.wrap(env, context.getPageTotal()));
			contentHtml = pageContents[context.getPageIndex() - 1];
		}
		return Map.of(TemplateVariable_ArticleContent, this.wrap(env, contentHtml));
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
