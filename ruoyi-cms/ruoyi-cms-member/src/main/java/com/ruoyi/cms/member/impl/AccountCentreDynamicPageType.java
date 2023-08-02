package com.ruoyi.cms.member.impl;

import com.ruoyi.cms.member.publishpipe.PublishPipeProp_AccountCentreTemplate;
import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.core.IDynamicPageType;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.member.domain.vo.MemberCache;
import com.ruoyi.member.service.IMemberStatDataService;
import com.ruoyi.member.util.MemberUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 个人中心页
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IDynamicPageType.BEAN_PREFIX + AccountCentreDynamicPageType.TYPE)
public class AccountCentreDynamicPageType implements IDynamicPageType {

    public static final String TYPE = "AccountCentre";

    public static final String REQUEST_PATH = "account/{memberId}";

    private final IMemberStatDataService memberStatDataService;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "个人中心页";
    }

    @Override
    public String getRequestPath() {
        return REQUEST_PATH;
    }

    @Override
    public String getPublishPipeKey() {
        return PublishPipeProp_AccountCentreTemplate.KEY;
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
        Long siteId = MapUtils.getLong(parameters, "sid");
        Long memberId = MapUtils.getLong(parameters, "memberId");
        MemberCache member = this.memberStatDataService.getMemberCache(memberId);
        templateContext.getVariables().put("Member", member);
        templateContext.getVariables().put("MemberResourcePrefix", MemberUtils.getMemberResourcePrefix(templateContext.isPreview()));
        templateContext.getVariables().put("Request", parameters);
        templateContext.setPageIndex(MapUtils.getIntValue(parameters, "page", 1));

        String link = "account/" + memberId + "?type=" + parameters.get("type");
        if (templateContext.isPreview()) {
            link += "&sid=" + siteId + "&pp=" + templateContext.getPublishPipeCode() + "&preview=true";
        }
        templateContext.setFirstFileName(link);
        templateContext.setOtherFileName(link + "&page=" + TemplateContext.PlaceHolder_PageNo);
    }
}
