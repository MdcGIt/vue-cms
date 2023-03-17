package com.ruoyi.contentcore.util;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.ContentCoreConsts;
import com.ruoyi.contentcore.config.CMSConfig;
import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.core.impl.InternalDataType_Site;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.fixed.config.BackendContext;

public class SiteUtils {

    /**
     * 获取站点指定发布通道根目录路径，静态化文件目录
     *
     * @param site
     * @param publishPipeCode
     * @return
     */
    public static String getSiteRoot(CmsSite site, String publishPipeCode) {
        return CMSConfig.getResourceRoot() + getSitePublishPipePath(site.getPath(), publishPipeCode);
    }

    /**
     * 获取站点发布通道访问链接前缀
     *
     * @return
     */
    public static String getPublishPipePrefix(CmsSite site, String publishPipeCode, boolean isPreview) {
        if (isPreview) {
            return CMSConfig.getResourcePreviewPrefix()
                    + getSitePublishPipePath(site.getPath(), publishPipeCode);
        }
        return site.getUrl(publishPipeCode);
    }

    /**
     * 站点发布通道相对资源根目录路径 = 站点目录名 + "_" + 发布通道编码 + "/"
     *
     * @param sitePath
     * @param publishPipeCode
     * @return
     */
    public static String getSitePublishPipePath(String sitePath, String publishPipeCode) {
        return sitePath + "_" + publishPipeCode + StringUtils.SLASH;
    }

    /**
     * 获取站点资源文件根目录
     *
     * @param sitePath
     * @return
     */
    public static String getSiteResourceRoot(String sitePath) {
        return CMSConfig.getResourceRoot() + getSiteResourcePath(sitePath);
    }

    public static String getSiteResourceRoot(CmsSite site) {
        return getSiteResourceRoot(site.getPath());
    }

    /**
     * 获取站点资源文件访问链接前缀
     *
     * @param site
     * @param isPreview
     * @return
     */
    public static String getResourcePrefix(CmsSite site) {
    	String prefix = site.getResourceUrl();
    	if (StringUtils.isEmpty(prefix)) {
    		prefix = CMSConfig.getResourcePreviewPrefix() + getSiteResourcePath(site.getPath());
    	}
        return prefix;
    }

    /**
     * 获取站点资源相对资源根目录路径
     *
     * @param sitePath
     * @return
     */
    public static String getSiteResourcePath(String sitePath) {
        return sitePath + StringUtils.SLASH;
    }

    /**
     * 获取站点访问链接
     *
     * @param site
     * @param publishPipeCode
     * @param isPreview
     * @return
     */
    public static String getSiteLink(CmsSite site, String publishPipeCode, boolean isPreview) {
        if (isPreview) {
			String previewPath = IInternalDataType.getPreviewPath(InternalDataType_Site.ID,
					site.getSiteId(), publishPipeCode, 1);
			return BackendContext.getValue() + previewPath;
        }
        return site.getUrl(publishPipeCode);
    }
    
    /**
	 * 获取模板相对项目资源根目录（ResourceRoot）的路径
	 * 
	 * @param site
	 * @param template 模板文件路径，相对于站点模板目录路径
	 * @param publishPipeCode 发布通道编码
	 * @return
	 */
    public static String getTemplateName(CmsSite site, String publishPipeCode, String template) {
    	return site.getPath() + "_" + publishPipeCode + StringUtils.SLASH + ContentCoreConsts.TemplateDirectory + template;
    }
    
    public static String getSiteTemplatePath(String sitePath, String publishPipeCode) {
    	return sitePath + "_" + publishPipeCode + StringUtils.SLASH + ContentCoreConsts.TemplateDirectory; 
    }
}
