package com.ruoyi.link.template.tag;

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
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.link.domain.CmsLinkGroup;
import com.ruoyi.link.service.ILinkGroupService;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CmsLinkGroupTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_link_group";
	public final static String NAME = "{FREEMARKER.TAG.NAME." + TAG_NAME + "}";
	public final static String DESC = "{FREEMARKER.TAG.DESC." + TAG_NAME + "}";

	private final ILinkGroupService linkGroupService;
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("code", false, TagAttrDataType.STRING, "友链分组编码") );
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException {
		String code = MapUtils.getString(attrs, "code");
		Long siteId = FreeMarkerUtils.evalLongVariable(env, "Site.siteId");
		LambdaQueryWrapper<CmsLinkGroup> q = new LambdaQueryWrapper<CmsLinkGroup>()
				.eq(siteId != null && siteId > 0, CmsLinkGroup::getSiteId, siteId)
				.eq(StringUtils.isNotEmpty(code), CmsLinkGroup::getCode, code);
		 Page<CmsLinkGroup> pageResult = this.linkGroupService.page(new Page<>(pageIndex, size, page), q);
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
