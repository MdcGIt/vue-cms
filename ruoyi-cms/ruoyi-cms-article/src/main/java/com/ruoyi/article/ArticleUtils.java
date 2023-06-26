package com.ruoyi.article;

import com.ruoyi.common.async.AsyncTaskManager;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.core.impl.InternalDataType_PageWidget;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsPageWidget;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.properties.EnableSSIProperty;
import com.ruoyi.contentcore.service.*;
import com.ruoyi.contentcore.template.tag.CmsIncludeTag;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.ruoyi.contentcore.util.ContentUtils;
import com.ruoyi.contentcore.util.PageWidgetUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleUtils {

    private static final ISiteService siteService = SpringUtils.getBean(ISiteService.class);

    private static final ICatalogService catalogService = SpringUtils.getBean(ICatalogService.class);

    private static final IContentService contentService = SpringUtils.getBean(IContentService.class);

    private static final IPageWidgetService pageWidgetService = SpringUtils.getBean(IPageWidgetService.class);

    private static final IPublishService publishService = SpringUtils.getBean(IPublishService.class);


    /**
     * 内容扩展模板内容占位符标签匹配
     */
    public static final Pattern ContentExPlaceholderTagPattern = Pattern.compile("<img[^>]+ex_cid\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * 替换文章正文内容扩展模板占位符，需要在处理图片链接前处理
     *
     * 占位符：<img src="/UEditorPlus/themes/default/images/spacer.gif" ex_cid="{content.id}" title="{content.title}" class="img_group_placeholder" />
     * 替换为ssi引用标签
     *
     * @return
     */
    public static String dealContentEx(String articleBody, String publishPipeCode, boolean isPreview) {
        if (StringUtils.isBlank(articleBody)) {
            return articleBody;
        }
        Matcher matcher = ContentExPlaceholderTagPattern.matcher(articleBody);
        int lastEndIndex = 0;
        StringBuilder sb = new StringBuilder();
        while (matcher.find(lastEndIndex)) {
            int s = matcher.start();
            sb.append(articleBody.substring(lastEndIndex, s));

            String placeholderImgTag = matcher.group();
            String contentId = matcher.group(1);
            try {
                CmsContent _content = contentService.getById(contentId);
                if (_content != null) {
                    CmsSite site = siteService.getSite(_content.getSiteId());
                    CmsCatalog catalog = catalogService.getCatalog(_content.getCatalogId());
                    if (isPreview) {
                        // 获取预览内容
                        placeholderImgTag = publishService.getContentExPageData(_content, publishPipeCode, true);
                    } else {
                        boolean ssiEnabled = EnableSSIProperty.getValue(site.getConfigProps());
                        if (catalog.isStaticize() && ssiEnabled) {
                            String staticFilePath = ContentUtils.getContentExPath(site, catalog, _content, publishPipeCode);
                            placeholderImgTag = StringUtils.messageFormat(CmsIncludeTag.SSI_INCLUDE_TAG, "/" + staticFilePath);
                        } else {
                            // 获取浏览内容
                            placeholderImgTag = publishService.getContentExPageData(_content, publishPipeCode, false);;
                        }
                    }
                } else {
                    placeholderImgTag = StringUtils.EMPTY;
                }
            } catch (Exception e1) {
                AsyncTaskManager.addErrMessage("Replace content-ex placeholder failed: " + contentId);
            }
            sb.append(placeholderImgTag);
            lastEndIndex = matcher.end();
        }
        sb.append(articleBody.substring(lastEndIndex));
        return sb.toString();
    }

    /**
     * 页面部件占位符标签匹配
     */
    public static final Pattern PageWidgetImgHtmlTagPattern = Pattern.compile("<img[^>]+pw_code\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

    /**
     * 替换文章正文页面部件占位符，需要在处理图片链接前处理
     *
     * 占位符：<img src="/UEditorPlus/themes/default/images/spacer.gif" pw_code="{pageWidget.code}" title="{pageWidget.name}" class="pw_placeholder" />
     * 替换为ssi引用标签
     *
     * @return
     */
    public static String dealPageWidget(CmsContent content, String articleBody, boolean isPreview) {
        if (StringUtils.isBlank(articleBody)) {
            return articleBody;
        }
        CmsSite site = siteService.getSite(content.getSiteId());
        CmsCatalog catalog = catalogService.getCatalog(content.getCatalogId());
        Matcher matcher = PageWidgetImgHtmlTagPattern.matcher(articleBody);
        int lastEndIndex = 0;
        StringBuilder sb = new StringBuilder();
        while (matcher.find(lastEndIndex)) {
            int s = matcher.start();
            sb.append(articleBody.substring(lastEndIndex, s));

            String placeholderImgTag = matcher.group();
            String pageWidgetCode = matcher.group(1);
            try {
                CmsPageWidget pw = pageWidgetService.getPageWidget(site.getSiteId(), pageWidgetCode);
                if (pw != null) {
                    if (isPreview) {
                        IInternalDataType internalDataType = ContentCoreUtils.getInternalDataType(InternalDataType_PageWidget.ID);
                        String pageData = internalDataType.getPageData(new IInternalDataType.RequestData(pw.getPageWidgetId(),
                                1, pw.getPublishPipeCode(), true, null));
                        placeholderImgTag = pageData;
                    } else {
                        boolean ssiEnabled = EnableSSIProperty.getValue(site.getConfigProps());
                        if (catalog.isStaticize() && ssiEnabled) {
                            String staticFileName = PageWidgetUtils.getStaticFileName(pw, site.getStaticSuffix(pw.getPublishPipeCode()));
                            String staticFilePath = pw.getPath() + staticFileName;
                            placeholderImgTag = StringUtils.messageFormat(CmsIncludeTag.SSI_INCLUDE_TAG, "/" + staticFilePath);
                        } else {
                            IInternalDataType internalDataType = ContentCoreUtils.getInternalDataType(InternalDataType_PageWidget.ID);
                            String pageData = internalDataType.getPageData(new IInternalDataType.RequestData(pw.getPageWidgetId(),
                                    1, pw.getPublishPipeCode(), false, null));
                            placeholderImgTag = pageData;
                        }
                    }
                }
            } catch (Exception e1) {
                AsyncTaskManager.addErrMessage("Replace page widget placeholder failed: " + pageWidgetCode);
            }
            sb.append(placeholderImgTag);
            lastEndIndex = matcher.end();
        }
        sb.append(articleBody.substring(lastEndIndex));
        return sb.toString();
    }
}
