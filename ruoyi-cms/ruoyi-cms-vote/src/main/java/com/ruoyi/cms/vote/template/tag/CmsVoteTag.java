package com.ruoyi.cms.vote.template.tag;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.cms.vote.service.ICmsVoteService;
import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.staticize.tag.AbstractListTag;
import com.ruoyi.common.staticize.tag.TagAttr;
import com.ruoyi.vote.domain.Vote;
import com.ruoyi.vote.service.IVoteService;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CmsVoteTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_vote";
	public final static String NAME = "调查投票列表标签";

	private final IVoteService voteService;

	private final ICmsVoteService cmsVoteService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("siteid", false, TagAttrDataType.STRING, "站点ID"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		long siteId = MapUtils.getLongValue(attrs, "siteid", FreeMarkerUtils.evalLongVariable(env, "Site.siteId"));
		String voteSource = this.cmsVoteService.getVoteSource(siteId);
		Page<Vote> pageResult = this.voteService.lambdaQuery().eq(Vote::getSource, voteSource)
				.page(new Page<>(pageIndex, size, page));
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
		return "获取当前站点调查投票接数据列表，内嵌<#list DataList as vote>${vote.title}</#list>遍历数据";
	}
}
