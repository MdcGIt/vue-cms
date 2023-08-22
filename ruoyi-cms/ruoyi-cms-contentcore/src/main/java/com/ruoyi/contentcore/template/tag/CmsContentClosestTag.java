package com.ruoyi.contentcore.template.tag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.staticize.tag.AbstractListTag;
import com.ruoyi.common.staticize.tag.TagAttr;
import com.ruoyi.common.staticize.tag.TagAttrOption;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.dto.ContentDTO;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CmsContentClosestTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_content_closest";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";


	final static String TagAttr_ContentId = "contentid";

	final static String TagAttr_Sort = "sort";

	final static String TagAttr_Type = "type";

	private final IContentService contentService;

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		boolean isNext = TypeTagAttr.isNext(attrs.get(TagAttr_Type));
		String sort = attrs.get(TagAttr_Sort);
		Long contentId = MapUtils.getLong(attrs, TagAttr_ContentId);

		CmsContent content = this.contentService.getById(contentId);
		Assert.notNull(content, () -> new TemplateException(StringUtils.messageFormat("Tag attr[contentid={0}] data not found.", contentId), env));

		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
				.eq(CmsContent::getCatalogId, content.getCatalogId());
		if (CmsContentTag.SortTagAttr.isRecent(sort)) {
			q.gt(isNext, CmsContent::getPublishDate, content.getPublishDate());
			q.lt(!isNext, CmsContent::getPublishDate, content.getPublishDate());
			q.orderBy(true, isNext, CmsContent::getPublishDate);
		} else if(CmsContentTag.SortTagAttr.isViews(sort)) {
			q.gt(isNext, CmsContent::getViewCount, content.getViewCount());
			q.lt(!isNext, CmsContent::getViewCount, content.getViewCount());
			q.orderBy(true, isNext, CmsContent::getViewCount);
		} else {
			q.gt(isNext, CmsContent::getSortFlag, content.getSortFlag());
			q.lt(!isNext, CmsContent::getSortFlag, content.getSortFlag());
			q.orderBy(true, isNext, CmsContent::getSortFlag);
		}
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		Page<CmsContent> pageResult = this.contentService.page(new Page<>(1, 1, false), q);
		if (pageResult.getRecords().isEmpty()) {
			return TagPageData.of(List.of(), 0);
		}
		List<ContentDTO> dataList = pageResult.getRecords().stream().map(c -> {
			ContentDTO dto = ContentDTO.newInstance(c);
			dto.setLink(this.contentService.getContentLink(c, 1, context.getPublishPipeCode(), context.isPreview()));
			dto.setLogoSrc(InternalUrlUtils.getActualUrl(c.getLogo(), context.getPublishPipeCode(), context.isPreview()));
			return dto;
		}).toList();
		return TagPageData.of(dataList, dataList.size());
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

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(TagAttr_ContentId, true, TagAttrDataType.INTEGER, "内容ID"));
		tagAttrs.add(new TagAttr(TagAttr_Type, true, TagAttrDataType.STRING, "类型", TypeTagAttr.toTagAttrOptions(), TypeTagAttr.Prev.name()));
		tagAttrs.add(new TagAttr(TagAttr_Sort, false, TagAttrDataType.STRING, "排序方式", CmsContentTag.SortTagAttr.toTagAttrOptions(), CmsContentTag.SortTagAttr.Default.name()));
		return tagAttrs;
	}

	enum TypeTagAttr {
		Prev("上一篇"), Next("下一篇");

		private final String desc;

		TypeTagAttr(String desc){
			this.desc = desc;
		}

		public static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(Prev.name(), Prev.desc),
					new TagAttrOption(Next.name(), Next.desc)
			);
		}

		public static boolean isNext(String v) {
			return Next.name().equalsIgnoreCase(v);
		}
	}
}
