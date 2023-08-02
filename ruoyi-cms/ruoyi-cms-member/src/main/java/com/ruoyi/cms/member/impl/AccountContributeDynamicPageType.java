package com.ruoyi.cms.member.impl;

import com.ruoyi.article.domain.CmsArticleDetail;
import com.ruoyi.article.service.IArticleService;
import com.ruoyi.cms.member.domain.vo.ContributeArticleVO;
import com.ruoyi.cms.member.publishpipe.PublishPipeProp_MemberContributeTemplate;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IDynamicPageType;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.member.domain.Member;
import com.ruoyi.member.domain.vo.MemberCache;
import com.ruoyi.member.service.IMemberService;
import com.ruoyi.member.service.IMemberStatDataService;
import com.ruoyi.member.util.MemberUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 会员投稿页
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IDynamicPageType.BEAN_PREFIX + AccountContributeDynamicPageType.TYPE)
public class AccountContributeDynamicPageType implements IDynamicPageType {

    public static final String TYPE = "AccountContribute";

    public static final String REQUEST_PATH = "account/contribute";

    private final IMemberStatDataService memberStatDataService;

    private final IMemberService memberService;

    private final IContentService contentService;

    private final IArticleService articleService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "会员投稿页";
    }

    @Override
    public String getRequestPath() {
        return REQUEST_PATH;
    }

    @Override
    public String getPublishPipeKey() {
        return PublishPipeProp_MemberContributeTemplate.KEY;
    }

    @Override
    public void validate(Map<String, String> parameters) {
        Long memberId = MapUtils.getLong(parameters, "memberId");

        MemberCache member = this.memberStatDataService.getMemberCache(memberId);
        if (Objects.isNull(member)) {
            throw new RuntimeException("Member not found: " + memberId);
        }
    }

    @Override
    public void initTemplateData(Map<String, String> parameters, TemplateContext templateContext) {
        Long memberId = MapUtils.getLong(parameters, "memberId");
        Member member = this.memberService.getById(memberId);
        templateContext.getVariables().put("Member", member);
        templateContext.getVariables().put("MemberResourcePrefix", MemberUtils.getMemberResourcePrefix(templateContext.isPreview()));
        templateContext.getVariables().put("Request", ServletUtils.getParameters());

        Long contentId = MapUtils.getLong(parameters, "cid", 0L);
        if (IdUtils.validate(contentId)) {
            CmsContent content = this.contentService.getById(contentId);
            CmsArticleDetail articleDetail = this.articleService.getById(content.getContentId());
            ContributeArticleVO article = ContributeArticleVO.newInstance(content, articleDetail);
            if (StringUtils.isNotEmpty(content.getLogo())) {
                article.setLogoSrc(InternalUrlUtils.getActualUrl(article.getLogo(), templateContext.getPublishPipeCode(), templateContext.isPreview()));
            }
            article.setContentHtml(InternalUrlUtils.dealResourceInternalUrl(article.getContentHtml()));
            templateContext.getVariables().put("Article", article);
        } else {
            templateContext.getVariables().put("Article", new ContributeArticleVO());
        }
    }
}
