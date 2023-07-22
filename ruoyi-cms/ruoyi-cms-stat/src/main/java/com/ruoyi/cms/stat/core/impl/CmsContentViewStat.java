package com.ruoyi.cms.stat.core.impl;

import com.ruoyi.cms.stat.core.CmsStat;
import com.ruoyi.cms.stat.domain.CmsSiteVisitLog;
import com.ruoyi.contentcore.service.impl.ContentDynamicDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 内容动态浏览量统计
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component
@RequiredArgsConstructor
public class CmsContentViewStat implements CmsStat {

    private final ContentDynamicDataService contentDynamicDataService;

    @Override
    public void deal(final CmsSiteVisitLog log) {
        this.contentDynamicDataService.increaseViewCount(log.getContentId());
    }
}
