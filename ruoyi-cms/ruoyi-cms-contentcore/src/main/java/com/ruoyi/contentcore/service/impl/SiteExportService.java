package com.ruoyi.contentcore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.async.AsyncTask;
import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.contentcore.ContentCoreConsts;
import com.ruoyi.contentcore.config.CMSConfig;
import com.ruoyi.contentcore.domain.*;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.fixed.dict.ContentStatus;
import com.ruoyi.contentcore.listener.event.SiteExportEvent;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.IResourceService;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.contentcore.util.TemplateUtils;
import jodd.io.ZipBuilder;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class SiteExportService implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final AsyncTaskManager asyncTaskManager;

    private final IPublishPipeService publishPipeService;

    private final ICatalogService catalogService;

    private final IContentService contentService;

    private final IResourceService resourceService;

    public AsyncTask exportSite(CmsSite site, List<String> directories) {
        String taskId = "SiteExport-" + site.getPath();
        AsyncTask task = asyncTaskManager.getTask(taskId);
        Assert.isTrue(task == null || !task.isAlive(), ContentCoreErrorCode.SITE_EXPORT_TASK_EXISTS::exception);

        AsyncTask asyncTask = new AsyncTask() {

            @Override
            public void run0() throws Exception {
                String siteResourceRoot = SiteUtils.getSiteResourceRoot(site);
                String zipFile = siteResourceRoot + "site_exporter.zip";
                ZipBuilder zipBuilder = ZipBuilder.createZipFile(new File(zipFile));
                final String prefix = "wwwroot/";
                // 资源文件
                File resourceFiles = new File(siteResourceRoot + "resources/");
                if (resourceFiles.exists()) {
                    zipBuilder.add(resourceFiles)
                            .path(prefix + site.getPath() + "/resources/")
                            .recursive()
                            .save();
                }
                // 发布通道文件
                publishPipeService.getAllPublishPipes(site.getSiteId()).forEach(pp -> {
                    String siteRoot = SiteUtils.getSiteRoot(site, pp.getCode());
                    try {
                        File jsFiles = new File(siteRoot + "js");
                        if (jsFiles.exists()) {
                            zipBuilder.add(jsFiles)
                                    .path(prefix + SiteUtils.getSitePublishPipePath(site.getPath(), pp.getCode()) + "js/")
                                    .recursive()
                                    .save();
                        }
                        File cssFiles = new File(siteRoot + "css");
                        if (cssFiles.exists()) {
                            zipBuilder.add(cssFiles)
                                    .path(prefix + SiteUtils.getSitePublishPipePath(site.getPath(), pp.getCode()) + "css/")
                                    .recursive()
                                    .save();
                        }
                        File imageFiles = new File(siteRoot + "images");
                        if (imageFiles.exists()) {
                            zipBuilder.add(imageFiles)
                                    .path(prefix + SiteUtils.getSitePublishPipePath(site.getPath(), pp.getCode()) + "images/")
                                    .recursive()
                                    .save();
                        }
                        File assetsFiles = new File(siteRoot + "assets");
                        if (assetsFiles.exists()) {
                            zipBuilder.add(assetsFiles)
                                    .path(prefix + SiteUtils.getSitePublishPipePath(site.getPath(), pp.getCode()) + "assets/")
                                    .recursive()
                                    .save();
                        }
                        File templateFiles = new File(siteRoot + ContentCoreConsts.TemplateDirectory);
                        if (templateFiles.exists()) {
                            zipBuilder.add(templateFiles)
                                    .path(prefix + SiteUtils.getSitePublishPipePath(site.getPath(), pp.getCode())
                                            + ContentCoreConsts.TemplateDirectory)
                                    .recursive()
                                    .save();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                // 数据库数据
                long limit = 1000;
                // cms_catalog
                {
                    long total = catalogService.count(new LambdaQueryWrapper<CmsCatalog>()
                            .eq(CmsCatalog::getSiteId, site.getSiteId()));
                    int pageNumber = 1;
                    long offset = 0;
                    while (total > 0) {
                        LambdaQueryWrapper<CmsCatalog> q = new LambdaQueryWrapper<CmsCatalog>()
                                .eq(CmsCatalog::getSiteId, site.getSiteId())
                                .gt(CmsCatalog::getCatalogId, offset)
                                .orderByAsc(CmsCatalog::getCatalogId);
                        Page<CmsCatalog> page = catalogService.page(new Page<>(1, limit, false), q);
                        String json = JacksonUtils.to(page.getRecords());
                        zipBuilder.add(json.getBytes(StandardCharsets.UTF_8))
                                .path("db/" + CmsCatalog.TABLE_NAME + "/page" + pageNumber + ".json")
                                .save();
                        offset = page.getRecords().get(page.getRecords().size() - 1).getCatalogId();
                        total -= page.getRecords().size();
                    }
                }
                // cms_content
                {
                    long total = contentService.count(new LambdaQueryWrapper<CmsContent>()
                            .eq(CmsContent::getSiteId, site.getSiteId()));
                    int pageNumber = 1;
                    long offset = 0;
                    while (total > 0) {
                        LambdaQueryWrapper<CmsContent> q = new LambdaQueryWrapper<CmsContent>()
                                .eq(CmsContent::getSiteId, site.getSiteId())
                                .gt(CmsContent::getContentId, offset)
                                .orderByAsc(CmsContent::getContentId);
                        Page<CmsContent> page = contentService.page(new Page<>(1, limit, false), q);
                        String json = JacksonUtils.to(page.getRecords());
                        zipBuilder.add(json.getBytes(StandardCharsets.UTF_8))
                                .path("db/" + CmsContent.TABLE_NAME + "/page" + pageNumber + ".json")
                                .save();
                        offset = page.getRecords().get(page.getRecords().size() - 1).getContentId();
                        total -= page.getRecords().size();
                    }
                    offset = 0;
                }
                // cms_resource
                {
                    long total = resourceService.count(new LambdaQueryWrapper<CmsResource>()
                            .eq(CmsResource::getSiteId, site.getSiteId()));
                    int pageNumber = 1;
                    long offset = 0;
                    while (total > 0) {
                        LambdaQueryWrapper<CmsResource> q = new LambdaQueryWrapper<CmsResource>()
                                .eq(CmsResource::getSiteId, site.getSiteId())
                                .gt(CmsResource::getResourceId, offset)
                                .orderByAsc(CmsResource::getResourceId);
                        Page<CmsResource> page = resourceService.page(new Page<>(1, limit, false), q);
                        String json = JacksonUtils.to(page.getRecords());
                        zipBuilder.add(json.getBytes(StandardCharsets.UTF_8))
                                .path("db/" + CmsResource.TABLE_NAME + "/page" + pageNumber + ".json")
                                .save();
                        offset = page.getRecords().get(page.getRecords().size() - 1).getResourceId();
                        total -= page.getRecords().size();
                    }
                }
                // cms_publishpipe
                {
                    long total = publishPipeService.count(new LambdaQueryWrapper<CmsPublishPipe>()
                            .eq(CmsPublishPipe::getSiteId, site.getSiteId()));
                    int pageNumber = 1;
                    long offset = 0;
                    while (total > 0) {
                        LambdaQueryWrapper<CmsPublishPipe> q = new LambdaQueryWrapper<CmsPublishPipe>()
                                .eq(CmsPublishPipe::getSiteId, site.getSiteId())
                                .gt(CmsPublishPipe::getPublishpipeId, offset)
                                .orderByAsc(CmsPublishPipe::getPublishpipeId);
                        Page<CmsPublishPipe> page = publishPipeService.page(new Page<>(1, limit, false), q);
                        String json = JacksonUtils.to(page.getRecords());
                        zipBuilder.add(json.getBytes(StandardCharsets.UTF_8))
                                .path("db/" + CmsPublishPipe.TABLE_NAME + "/page" + pageNumber + ".json")
                                .save();
                        offset = page.getRecords().get(page.getRecords().size() - 1).getPublishpipeId();
                        total -= page.getRecords().size();
                    }
                }
                applicationContext.publishEvent(new SiteExportEvent(this, site, zipBuilder));
                zipBuilder.toZipFile();
            }
        };
        asyncTask.setTaskId(taskId);
        asyncTask.setType("SiteExport");
        this.asyncTaskManager.execute(asyncTask);
        return asyncTask;
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
