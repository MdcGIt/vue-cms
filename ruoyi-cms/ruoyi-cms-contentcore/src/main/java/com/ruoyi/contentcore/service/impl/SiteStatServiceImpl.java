package com.ruoyi.contentcore.service.impl;

import java.util.Map;
import java.util.stream.Collectors;

import com.ruoyi.contentcore.domain.*;
import com.ruoyi.contentcore.service.ITemplateService;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ruoyi.contentcore.domain.vo.SiteStatVO;
import com.ruoyi.contentcore.mapper.CmsContentMapper;
import com.ruoyi.contentcore.mapper.CmsResourceMapper;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.ISiteStatService;
import com.ruoyi.contentcore.util.ContentCoreUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Service
public class SiteStatServiceImpl implements ISiteStatService {

    private final ICatalogService catalogService;

    private final CmsContentMapper contentMapper;

    private final CmsResourceMapper resourceMapper;

    private final ITemplateService templateService;

    /**
     * 获取站点相关资源统计数据
     *
     * @param site
     * @return
     */
    @Override
    public SiteStatVO getSiteStat(CmsSite site) {
        SiteStatVO vo = new SiteStatVO();
        vo.setSiteId(site.getSiteId());

        // 栏目数量
        long catalogCount = this.catalogService
                .count(new LambdaQueryWrapper<CmsCatalog>().eq(CmsCatalog::getSiteId, site.getSiteId()));
        vo.setCatalogCount(catalogCount);
        // 内容数量
        long contentCount = this.contentMapper
                .selectCount(new LambdaQueryWrapper<CmsContent>().eq(CmsContent::getSiteId, site.getSiteId()));
        vo.setContentCount(contentCount);
        // 内容分类统计
        Map<String, Long> countContentMap = this.contentMapper.countContentGroupByType(site.getSiteId()).stream()
                .collect(Collectors.toMap(SiteStatData::getDataKey, SiteStatData::getDataValue));
        ContentCoreUtils.getContentTypes().values().forEach(ct -> {
            if (!countContentMap.containsKey(ct.getId())) {
                countContentMap.put(ct.getId(), 0L);
            }
        });
        vo.setContentDetails(countContentMap);
        // 资源数量
        long resourceCount = this.resourceMapper
                .selectCount(new LambdaQueryWrapper<CmsResource>().eq(CmsResource::getSiteId, site.getSiteId()));
        vo.setResourceCount(resourceCount);
        // 资源分类统计
        Map<String, Long> countResourceMap = this.resourceMapper.countResourceGroupByType(site.getSiteId()).stream()
                .collect(Collectors.toMap(SiteStatData::getDataKey, SiteStatData::getDataValue));
        ContentCoreUtils.getResourceTypes().forEach(rt -> {
            if (!countResourceMap.containsKey(rt.getId())) {
                countResourceMap.put(rt.getId(), 0L);
            }
        });
        vo.setResourceDetails(countResourceMap);
        // 模板数量
        long templateCount = this.templateService.count(new LambdaQueryWrapper<CmsTemplate>()
                .eq(CmsTemplate::getSiteId, site.getSiteId()));
        vo.setTemplateCount(templateCount);
        return vo;
    }

    @Getter
    @Setter
    public static class SiteStatData {

        private String dataKey;

        private Long dataValue;
    }
}
