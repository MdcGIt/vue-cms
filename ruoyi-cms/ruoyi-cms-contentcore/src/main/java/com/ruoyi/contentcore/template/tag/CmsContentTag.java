package com.ruoyi.contentcore.template.tag;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.staticize.tag.AbstractListTag;
import com.ruoyi.common.staticize.tag.TagAttr;
import com.ruoyi.common.staticize.tag.TagAttrOption;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.dto.ContentDTO;
import com.ruoyi.contentcore.fixed.dict.ContentAttribute;
import com.ruoyi.contentcore.fixed.dict.ContentStatus;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.template.exception.CatalogNotFoundException;
import com.ruoyi.contentcore.util.CatalogUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CmsContentTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_content";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final IContentService contentService;

	private final ICatalogService catalogService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("catalogid", false, TagAttrDataType.INTEGER, "栏目ID"));
		tagAttrs.add(new TagAttr("catalogalias", false, TagAttrDataType.STRING, "栏目别名"));
		tagAttrs.add(new TagAttr("level", false, TagAttrDataType.STRING, "数据获取范围，值为`Root`时忽略属性catalogid、catalogalias", LevelTagAttr.toTagAttrOptions(), LevelTagAttr.Current.name()));
		tagAttrs.add(new TagAttr("sort", false, TagAttrDataType.STRING, "排序方式", SortTagAttr.toTagAttrOptions(), SortTagAttr.Default.name()));
		tagAttrs.add(new TagAttr("hasattribute", false, TagAttrDataType.STRING, "包含内容属性，多个属性英文逗号分隔，属性定义见数据字典配置[cms_content_attribute]"));
		tagAttrs.add(new TagAttr("noattribute", false, TagAttrDataType.STRING, "不包含内容属性，多个属性英文逗号分隔，属性定义见数据字典配置[cms_content_attribute]"));
		tagAttrs.add(new TagAttr("status", false, TagAttrDataType.STRING, "状态，'-1'表示不限制状态，默认：已发布"));
		tagAttrs.add(new TagAttr("topflag", false, TagAttrDataType.BOOLEAN, "是否允许置顶，默认：允许", "true"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		CmsCatalog catalog = null;
		long catalogId = MapUtils.getLongValue(attrs, "catalogid");
		if (catalogId > 0) {
			catalog = this.catalogService.getCatalog(catalogId);
		}
		long siteId = FreeMarkerUtils.evalLongVariable(env, "Site.siteId");
		String alias = MapUtils.getString(attrs, "catalogalias");
		if (catalog == null && StringUtils.isNotEmpty(alias)) {
			catalog = this.catalogService.getCatalogByAlias(siteId, alias);
		}
		String level = MapUtils.getString(attrs, "level");
		if (!LevelTagAttr.isRoot(level) && Objects.isNull(catalog)) {
			throw new CatalogNotFoundException(getTagName(), catalogId, alias, env);
		}
		String condition = MapUtils.getString(attrs, TagAttr.AttrName_Condition);
		String status = MapUtils.getString(attrs, "status", ContentStatus.PUBLISHED);

		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<>();
		q.eq(CmsContent::getSiteId, siteId).eq(!"-1".equals(status), CmsContent::getStatus, ContentStatus.PUBLISHED);
		if (LevelTagAttr.isCurrent(level)) {
			q.eq(CmsContent::getCatalogId, catalog.getCatalogId());
		} else if (LevelTagAttr.isChild(level)) {
			q.likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors() + CatalogUtils.ANCESTORS_SPLITER);
		} else if (LevelTagAttr.isCurrentAndChild(level)) {
			q.likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors());
		}
		String hasAttribute = MapUtils.getString(attrs, "hasattribute");
		if (StringUtils.isNotEmpty(hasAttribute)) {
			int attrTotal = ContentAttribute.convertInt(hasAttribute.split(","));
			q.apply(attrTotal > 0, "attributes&{0}={1}", attrTotal, attrTotal);
		}
		String noAttribute = MapUtils.getString(attrs, "noattribute");
		if (StringUtils.isNotEmpty(noAttribute)) {
			String[] contentAttrs = noAttribute.split(",");
			int attrTotal = ContentAttribute.convertInt(contentAttrs);
			for (String attr : contentAttrs) {
				int bit = ContentAttribute.bit(attr);
				q.apply(bit > 0, "attributes&{0}<>{1}", attrTotal, bit);
			}
		}
		q.apply(StringUtils.isNotEmpty(condition), condition);
		String sortType = MapUtils.getString(attrs, "sort");
		q.orderByDesc(MapUtils.getBooleanValue(attrs, "topflag", true), CmsContent::getTopFlag);
		if (SortTagAttr.isRecent(sortType)) {
			q.orderByDesc(CmsContent::getPublishDate);
		} else if(SortTagAttr.isViews(sortType)) {
			q.orderByDesc(CmsContent::getViewCount);
		} else {
			q.orderByDesc(CmsContent::getSortFlag);
		}

		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		Page<CmsContent> pageResult = this.contentService.page(new Page<>(pageIndex, size, page), q);
		if (pageIndex > 1 & pageResult.getRecords().size() == 0) {
			throw new TemplateException("内容列表页码超出上限：" + pageIndex, env);
		}
		List<ContentDTO> list = new ArrayList<>();
		pageResult.getRecords().forEach(c -> {
			ContentDTO dto = ContentDTO.newInstance(c);
			dto.setLink(this.contentService.getContentLink(c, 1, context.getPublishPipeCode(), context.isPreview()));
			dto.setLogoSrc(InternalUrlUtils.getActualUrl(c.getLogo(), context.getPublishPipeCode(), context.isPreview()));
			list.add(dto);
		});
		return TagPageData.of(list, pageResult.getTotal());
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

	enum LevelTagAttr {
		Root("所有栏目"), Current("当前栏目"), Child("子栏目"), CurrentAndChild("当前栏目和子栏目");

		private final String desc;

		LevelTagAttr(String desc){
			this.desc = desc;
		}

		public static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(Root.name(), Root.desc),
					new TagAttrOption(Current.name(), Current.desc),
					new TagAttrOption(Child.name(), Child.desc),
					new TagAttrOption(CurrentAndChild.name(), CurrentAndChild.desc)
			);
		}

		public static boolean isCurrent(String v) {
			return Current.name().equalsIgnoreCase(v);
		}

		public static boolean isCurrentAndChild(String v) {
			return CurrentAndChild.name().equalsIgnoreCase(v);
		}

		public static boolean isChild(String v) {
			return Child.name().equalsIgnoreCase(v);
		}

		public static boolean isRoot(String v) {
			return Root.name().equalsIgnoreCase(v);
		}
	}

	enum SortTagAttr {
		Recent("发布时间降序"), Views("浏览量降序"), Default("排序字段降序（默认）");

		private final String desc;

		SortTagAttr(String desc){
			this.desc = desc;
		}

		public static List<TagAttrOption> toTagAttrOptions() {
			return List.of(
					new TagAttrOption(Recent.name(), Recent.desc),
					new TagAttrOption(Views.name(), Views.desc),
					new TagAttrOption(Default.name(), Default.desc)
			);
		}

		public static boolean isRecent(String v) {
			return Recent.name().equalsIgnoreCase(v);
		}

		public static boolean isViews(String v) {
			return Views.name().equalsIgnoreCase(v);
		}
	}
}
