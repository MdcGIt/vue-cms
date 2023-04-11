package com.ruoyi.contentcore.template.tag;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.staticize.tag.AbstractListTag;
import com.ruoyi.common.staticize.tag.TagAttr;
import com.ruoyi.contentcore.domain.CmsSiteProperty;
import com.ruoyi.contentcore.service.ISitePropertyService;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CmsSitePropertyTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_site_property";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	public final static String TagAttr_SiteId = "siteid";

	private final ISitePropertyService sitePropertyService;
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr(TagAttr_SiteId, false, TagAttrDataType.INTEGER, "站点ID，默认从模板变量中获取${Site.siteId}"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		long siteId = MapUtils.getLongValue(attrs, TagAttr_SiteId, FreeMarkerUtils.evalLongVariable(env, "Site.siteId"));
		if (siteId <= 0) {
			throw new TemplateException("站点数据ID异常：" + siteId, env);
		}
		LambdaQueryWrapper<CmsSiteProperty> q = new LambdaQueryWrapper<>();
		Page<CmsSiteProperty> pageResult = this.sitePropertyService.page(new Page<>(pageIndex, size, page), q);
		if (pageIndex > 1 & pageResult.getRecords().size() == 0) {
			throw new TemplateException("站点自定义属性数据列表页码超出上限：" + pageIndex, env);
		}
		return TagPageData.of(pageResult.getRecords(), pageResult.getTotal());
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
