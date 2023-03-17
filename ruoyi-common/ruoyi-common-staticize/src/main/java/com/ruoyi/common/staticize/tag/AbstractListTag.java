package com.ruoyi.common.staticize.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.StaticizeConstants;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.staticize.enums.TagAttrDataType;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public abstract class AbstractListTag extends AbstractTag {
	
	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = new ArrayList<>();
		tagAttrs.add(new TagAttr(TagAttr.AttrName_Page, false, TagAttrDataType.BOOLEAN, "FREEMARKER.TAG_ATTR.PAGE", "false"));
		tagAttrs.add(new TagAttr(TagAttr.AttrName_PageSize, false, TagAttrDataType.INTEGER, "FREEMARKER.TAG_ATTR.PAGE_SIZE", "20"));
		tagAttrs.add(new TagAttr(TagAttr.AttrName_Condition, false, TagAttrDataType.STRING, "FREEMARKER.TAG_ATTR.CONDITION"));
		return tagAttrs;
	}

	public abstract TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex) throws TemplateException;
	
	@Override
	public Map<String, TemplateModel> execute0(Environment env, Map<String, String> attrs)
			throws TemplateException, IOException {
		TemplateContext context = FreeMarkerUtils.getTemplateContext(env);
		// 标签属性处理
		boolean isPage = MapUtils.getBooleanValue(attrs, TagAttr.AttrName_Page);
		int size = MapUtils.getIntValue(attrs, TagAttr.AttrName_PageSize);
		// 当前模板分页索引
		if (isPage) {
			if (context.isPaged()) {
				throw new TemplateException("Tempalte page flag is already actived!", env); // 多个page=true的循环标签会覆盖分页信息
			}
			context.setPaged(true);
			context.setPageSize(size < 1 ? 20 : size); // 模板标签PageSize覆盖上下文初始值
		}
		TagPageData pageData = null;
		// 获取列表数据
		try {
			pageData = this.prepareData(env, attrs, isPage, size, isPage ? context.getPageIndex() : 1);
		} catch (Exception e) {
			throw new TemplateException(e, env);
		}
		// 设置分页模板全局分页信息
		if (isPage) {
			context.setPageTotal(pageData.total);
			context.setPageSize(size);
			env.setGlobalVariable(StaticizeConstants.TemplateVariable_PageSize, this.wrap(env, context.getPageSize()));
			env.setGlobalVariable(StaticizeConstants.TemplateVariable_PageTotal, this.wrap(env, context.getPageTotal()));
		}
		return Map.of(StaticizeConstants.TemplateVariable_DataList, this.wrap(env, pageData.list));
	}
	
	public static class TagPageData {
		List<?> list;
		long total;
		
		public static TagPageData of(List<?> list, long total) {
			TagPageData pageData = new TagPageData();
			pageData.list = list;
			pageData.total = total;
			return pageData;
		}
		public static TagPageData of(List<?> list) {
			TagPageData pageData = new TagPageData();
			pageData.list = list;
			pageData.total = list.size();
			return pageData;
		}
	}
}
