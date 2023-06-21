package com.ruoyi.contentcore.template.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.staticize.tag.AbstractListTag;
import com.ruoyi.common.staticize.tag.TagAttr;
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

@Component
@RequiredArgsConstructor
public class CmsContentTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_content";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	final static Map<String, String> AttrOptions_Level = Map.of("Root", "所有栏目", "Current", "当前栏目", "Child",
			"子栏目", "CurrentAndChild", "当前栏目和子栏目");

	final static Map<String, String> AttrOptions_Sort = Map.of("Recent", "发布时间降序", "Default", "（默认）排序字段降序");

	private final IContentService contentService;

	private final ICatalogService catalogService;
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("catalogid", false, TagAttrDataType.INTEGER, "栏目ID"));
		tagAttrs.add(new TagAttr("catalogalias", false, TagAttrDataType.STRING, "栏目别名"));
		TagAttr tagAttrLevel = new TagAttr("level", false, TagAttrDataType.STRING,
				"数据获取范围，值为`Root`时忽略属性catalogid、catalogalias", "Current");
		tagAttrLevel.setOptions(AttrOptions_Level);
		tagAttrs.add(tagAttrLevel);
		TagAttr tagAttrSort = new TagAttr("sort", false, TagAttrDataType.STRING, "排序方式", "Recent");
		tagAttrSort.setOptions(AttrOptions_Sort);
		tagAttrs.add(tagAttrSort);
		tagAttrs.add(new TagAttr("hasattribute", false, TagAttrDataType.STRING, "包含内容属性，多个属性英文逗号分隔，属性定义见数据字典配置[cms_content_attribute]"));
		tagAttrs.add(new TagAttr("noattribute", false, TagAttrDataType.STRING, "不包含内容属性，多个属性英文逗号分隔，属性定义见数据字典配置[cms_content_attribute]"));
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
		if (!"Root".equalsIgnoreCase(level) && Objects.isNull(catalog)) {
			throw new CatalogNotFoundException(getTagName(), catalogId, alias, env);
		}
		LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<>();
		q.eq(CmsContent::getSiteId, siteId).eq(CmsContent::getStatus, ContentStatus.PUBLISHED);
		if ("Current".equalsIgnoreCase(level)) {
			q.eq(CmsContent::getCatalogId, catalog.getCatalogId());
		} else if ("Child".equalsIgnoreCase(level)) {
			q.likeRight(CmsContent::getCatalogAncestors, catalog.getAncestors() + CatalogUtils.ANCESTORS_SPLITER);
		} else if ("CurrentAndChild".equalsIgnoreCase(level)) {
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
		String sortType = MapUtils.getString(attrs, "sort");
		if ("Recent".equalsIgnoreCase(sortType)) {
			q.orderByDesc(CmsContent::getPublishDate);
		} else {
			q.orderByDesc(Arrays.asList(CmsContent::getTopFlag, CmsContent::getSortFlag));
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
			dto.setAttributes(ContentAttribute.convertStr(c.getAttributes()));
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
}
