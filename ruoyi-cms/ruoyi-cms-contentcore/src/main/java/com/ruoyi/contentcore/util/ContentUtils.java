package com.ruoyi.contentcore.util;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.impl.PublishPipeProp_ContentExTemplate;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;

public class ContentUtils {

    /**
     * 获取内容扩展模板静态化相对站点路径
     *
     * @return
     */
    public static String getContentExPath(CmsSite site, CmsCatalog catalog, CmsContent content, String publishPipeCode) {
        String suffix = site.getStaticSuffix(publishPipeCode);
        return catalog.getPath() + getContextExFileName(content.getContentId(), suffix);
    }

    public static String getContextExFileName(Long contentId, String suffix) {
        return contentId + "_ex." + suffix;
    }

    public static String getContentExTemplate(CmsContent content, CmsCatalog catalog, String publishPipeCode) {
        String exTemplate = PublishPipeProp_ContentExTemplate.getValue(publishPipeCode,
                content.getPublishPipeProps());
        if (StringUtils.isEmpty(exTemplate)) {
            // 无内容独立扩展模板取栏目配置
            exTemplate = PublishPipeProp_ContentExTemplate.getValue(publishPipeCode, catalog.getPublishPipeProps());
        }
        return exTemplate;
    }
}
