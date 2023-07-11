package com.ruoyi.contentcore.util;

import java.util.Map;
import java.util.Objects;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.fixed.config.TemplateSuffix;
import org.springframework.stereotype.Component;

import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.ReflectASMUtils;
import com.ruoyi.contentcore.ContentCoreConsts;
import com.ruoyi.contentcore.core.IProperty.UseType;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsSite;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TemplateUtils {
	
	/**
	 * 模板变量：是否预览模式
	 */
	public final static String TemplateVariable_IsPreview = "IsPreview";

	/**
	 * 模板变量：发布通道静态化文件访问前缀
	 */
	public final static String TemplateVariable_Prefix = "Prefix";

	/**
	 * 模板变量：资源文件访问前缀
	 */
	public final static String TemplateVariable_ResourcePrefix = "ResourcePrefix";

	/**
	 * 模板变量：站点信息
	 */
	public final static String TemplateVariable_Site = "Site";

	/**
	 * 模板变量：栏目信息
	 */
	public final static String TemplateVariable_Catalog = "Catalog";

	/**
	 * 模板变量：内容信息
	 */
	public final static String TemplateVariable_Content = "Content";

	/**
	 * 模板变量：logo链接
	 */
	public final static String TemplateVariable_OBJ_LogoSrc = "logoSrc";

	/**
	 * 模板变量：访问地址
	 */
	public final static String TemplateVariable_OBJ_Link = "link";

	/**
	 * 添加站点数据到模板上线文变量中
	 *
	 * @param site
	 * @param context
	 */
	public static void addSiteVariables(CmsSite site, TemplateContext context) {
		// 站点属性
		Map<String, Object> mapSite = ReflectASMUtils.beanToMap(site);
		// 扩展属性
		Map<String, Object> configProps = ConfigPropertyUtils.parseConfigProps(site.getConfigProps(), UseType.Site);
		if (Objects.nonNull(configProps)) {
			configProps.entrySet().forEach(e -> {
				mapSite.put(ContentCoreConsts.ConfigPropFieldPrefix + e.getKey(), e.getValue());
			});
		}
		// 站点logo
		String siteLogo = InternalUrlUtils.getActualUrl(site.getLogo(), context.getPublishPipeCode(), context.isPreview());
		if (StringUtils.isNotEmpty(siteLogo)) {
			mapSite.put(TemplateVariable_OBJ_LogoSrc, siteLogo);
		}
		// 站点链接
		String siteLink = SiteUtils.getSiteLink(site, context.getPublishPipeCode(), context.isPreview());
		mapSite.put(TemplateVariable_OBJ_Link, siteLink);
		context.getVariables().put(TemplateVariable_Site, mapSite);
	}

	/**
	 * 添加栏目数据到模板上下文变量中
	 * 
	 * @param site
	 * @param catalog
	 * @param context
	 */
	public static void addCatalogVariables(CmsSite site, CmsCatalog catalog, TemplateContext context) {
		Map<String, Object> mapCatalog = ReflectASMUtils.beanToMap(catalog);
		// 扩展属性
		Map<String, Object> configProps = ConfigPropertyUtils.parseConfigProps(catalog.getConfigProps(), UseType.Catalog);
		if (Objects.nonNull(configProps)) {
			configProps.entrySet().forEach(e -> {
				mapCatalog.put(ContentCoreConsts.ConfigPropFieldPrefix + e.getKey(), e.getValue());
			});
		}
		// 栏目logo
		String catalogLogo = InternalUrlUtils.getActualUrl(catalog.getLogo(), context.getPublishPipeCode(), context.isPreview());
		if (StringUtils.isNotEmpty(catalogLogo)) {
			mapCatalog.put(TemplateVariable_OBJ_LogoSrc, catalogLogo);
		}
		// 栏目链接
		String catalogLink = CatalogUtils.getCatalogLink(site, catalog, 1, context.getPublishPipeCode(), context.isPreview());
		mapCatalog.put(TemplateVariable_OBJ_Link, catalogLink);
		String catalogListLink = CatalogUtils.getCatalogListLink(site, catalog, 1, context.getPublishPipeCode(), context.isPreview());
		mapCatalog.put("listLink", catalogListLink);
		context.getVariables().put(TemplateVariable_Catalog, mapCatalog);
	}

	/**
	 * 创建模板初始变量，包括全局变量和站点信息
	 *
	 * @param site
	 * @param context
	 * @return
	 */
	public static void initGlobalVariables(CmsSite site, TemplateContext context) {
		context.getVariables().put(TemplateVariable_IsPreview, context.isPreview());
		// 发布通道静态化文件访问前缀
		context.getVariables().put(TemplateVariable_Prefix, SiteUtils.getPublishPipePrefix(site, context.getPublishPipeCode(), context.isPreview()));
		// 资源文件访问前缀
		context.getVariables().put(TemplateVariable_ResourcePrefix, SiteUtils.getResourcePrefix(site, context.isPreview()));
		// 添加站点数据
		addSiteVariables(site, context);
	}

	/**
	 * 页面区块静态文件相对路径
	 *
	 * @param site
	 * @param publishPipeCode
	 * @param includeTemplateName 相对resourceRoot的模板路径
	 */
	public static String getIncludeRelativeStaticPath(CmsSite site, String publishPipeCode, String includeTemplateName) {
		String siteTemplatePath = SiteUtils.getSiteTemplatePath(site.getPath(), publishPipeCode);
		return "include/" + includeTemplateName.substring(siteTemplatePath.length(),
				includeTemplateName.length() - TemplateSuffix.getValue().length())
				+ "." + site.getStaticSuffix(publishPipeCode);
	}
}
