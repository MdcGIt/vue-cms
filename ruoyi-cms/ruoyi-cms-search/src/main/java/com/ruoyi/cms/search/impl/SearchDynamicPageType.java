package com.ruoyi.cms.search.impl;

import com.ruoyi.cms.search.publishpipe.PublishPipeProp_SearchTemplate;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IDynamicPageType;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 搜索结果页
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IDynamicPageType.BEAN_PREFIX + SearchDynamicPageType.TYPE)
public class SearchDynamicPageType implements IDynamicPageType {

    public static final String TYPE = "Search";

    public static final String REQUEST_PATH = "_search";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "搜索结果页";
    }

    @Override
    public String getRequestPath() {
        return REQUEST_PATH;
    }

    @Override
    public String getPublishPipeKey() {
        return PublishPipeProp_SearchTemplate.KEY;
    }

    @Override
    public void initTemplateData(Map<String, String> parameters, TemplateContext templateContext) {
        templateContext.getVariables().put("Request", ServletUtils.getParameters());
        String link = "_search?q=" + parameters.get("q");
        if (templateContext.isPreview()) {
            link += "&sid=" + parameters.get("sid") + "&pp=" + templateContext.getPublishPipeCode() + "&preview=true";
        }
        boolean onlyTitle = MapUtils.getBooleanValue(parameters, "ot", false);
        if (onlyTitle) {
            link += "&ot=true";
        }
        String contentType = parameters.get("ct");
        if (StringUtils.isNotEmpty(contentType)) {
            link += "&ct=" + contentType;
        }
        templateContext.setPageIndex(MapUtils.getIntValue(parameters, "page", 1));
        templateContext.setFirstFileName(link);
        templateContext.setOtherFileName(link + "&page=" + TemplateContext.PlaceHolder_PageNo);
    }
}
