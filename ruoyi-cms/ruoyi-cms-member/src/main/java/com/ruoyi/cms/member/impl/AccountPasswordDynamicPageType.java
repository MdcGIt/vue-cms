package com.ruoyi.cms.member.impl;

import com.ruoyi.cms.member.publishpipe.PublishPipeProp_MemberPasswordTemplate;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.core.IDynamicPageType;
import com.ruoyi.member.domain.Member;
import com.ruoyi.member.domain.vo.MemberCache;
import com.ruoyi.member.service.IMemberService;
import com.ruoyi.member.service.IMemberStatDataService;
import com.ruoyi.member.util.MemberUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 会员修改密码页
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IDynamicPageType.BEAN_PREFIX + AccountPasswordDynamicPageType.TYPE)
public class AccountPasswordDynamicPageType implements IDynamicPageType {

    public static final String TYPE = "AccountPassword";

    public static final String REQUEST_PATH = "account/password";

    public static final List<RequestArg> REQUEST_ARGS =  List.of(
            REQUEST_ARG_SITE_ID,
            REQUEST_ARG_PUBLISHPIPE_CODE,
            REQUEST_ARG_PREVIEW
    );

    private final IMemberStatDataService memberStatDataService;

    private final IMemberService memberService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "会员修改密码页";
    }

    @Override
    public String getRequestPath() {
        return REQUEST_PATH;
    }

    @Override
    public List<RequestArg> getRequestArgs() {
        return REQUEST_ARGS;
    }

    @Override
    public String getPublishPipeKey() {
        return PublishPipeProp_MemberPasswordTemplate.KEY;
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
    }
}
