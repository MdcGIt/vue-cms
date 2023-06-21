package com.ruoyi.contentcore.core;

import java.util.List;

/**
 * 导出站点接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ISiteExporter {

    public void export(SiteExportData data);

    /**
     * 站点导出数据，主要包含文件、数据库数据
     */
    public static class SiteExportData {

        private List<String> resources;
    }
}
