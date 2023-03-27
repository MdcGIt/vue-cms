package com.ruoyi.cms.vote.template.tag;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.common.staticize.enums.TagAttrDataType;
import com.ruoyi.common.staticize.tag.AbstractListTag;
import com.ruoyi.common.staticize.tag.TagAttr;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.vote.domain.Vote;
import com.ruoyi.vote.domain.VoteSubject;
import com.ruoyi.vote.service.IVoteService;
import com.ruoyi.vote.service.IVoteSubjectService;

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CmsVoteSubjectTag extends AbstractListTag {

	public final static String TAG_NAME = "cms_vote_subject";
	public final static String NAME = "调查投票主题列表标签";

	private final IVoteService voteService;

	private final IVoteSubjectService voteSubjectService;

	@Override
	public List<TagAttr> getTagAttrs() {
		List<TagAttr> tagAttrs = super.getTagAttrs();
		tagAttrs.add(new TagAttr("code", true, TagAttrDataType.STRING, "调查投票编码"));
		return tagAttrs;
	}

	@Override
	public TagPageData prepareData(Environment env, Map<String, String> attrs, boolean page, int size, int pageIndex)
			throws TemplateException {
		String code = MapUtils.getString(attrs, "code");
		if (StringUtils.isEmpty(code)) {
			throw new TemplateException("属性code不能为空", env);
		}
		Vote vote = this.voteService.lambdaQuery().eq(Vote::getCode, code).one();
		if (vote == null) {
			throw new TemplateException("获取调查投票数据失败：" + code, env);
		}

		List<VoteSubject> subjects = this.voteSubjectService.lambdaQuery().eq(VoteSubject::getVoteId, vote.getVoteId())
				.orderByAsc(VoteSubject::getSortFlag).list();
		return TagPageData.of(subjects);
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
		return "获取指定调查投票接主题数据列表，内嵌<#list DataList as subject>${subject.title}</#list>遍历数据";
	}
}
