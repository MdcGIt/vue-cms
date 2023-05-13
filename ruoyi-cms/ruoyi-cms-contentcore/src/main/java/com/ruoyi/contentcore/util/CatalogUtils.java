package com.ruoyi.contentcore.util;

import java.util.Objects;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.core.impl.CatalogType_Link;
import com.ruoyi.contentcore.core.impl.InternalDataType_Catalog;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.fixed.config.BackendContext;

public class CatalogUtils {
	
	/**
	 * 栏目父级ID分隔符
	 */
	public static final char ANCESTORS_SPLITER = ':';
	
	/**
	 * 生成指定栏目的子栏目用的祖级字符串
	 * 
	 * @param catalog
	 * @return
	 */
	public static String getCatalogAncestors(String parentAncestors, Long catalogId) {
		if (StringUtils.isNotEmpty(parentAncestors)) {
			return parentAncestors + ANCESTORS_SPLITER + catalogId;
		}
		return catalogId.toString();
	}

	public static String getCatalogAncestors(CmsCatalog parent, Long catalogId) {
		return getCatalogAncestors(Objects.isNull(parent) ? null : parent.getAncestors(), catalogId);
	}
	
	/**
	 * 获取指定栏目所属顶级栏目ID
	 * 
	 * @param catalog
	 * @return
	 */
	public static Long getTopCatalog(CmsCatalog catalog) {
		if (catalog.getCatalogId().toString().equals(catalog.getAncestors())) {
			return catalog.getCatalogId();
		}
		return Long.valueOf(catalog.getAncestors().substring(0, catalog.getAncestors().indexOf(ANCESTORS_SPLITER)));
	}

	/**
	 * 获取栏目访问链接
	 *
	 * @param site
	 * @param catalog
	 * @param pageIndex
	 * @param publishPipeCode
	 * @param isPreview
	 * @return
	 */
	public static String getCatalogLink(CmsSite site, CmsCatalog catalog, int pageIndex, String publishPipeCode, boolean isPreview) {
		if (catalog.getCatalogType().equals(CatalogType_Link.ID)) {
			return InternalUrlUtils.getActualUrl(catalog.getRedirectUrl(), publishPipeCode, isPreview);
		}
		if (isPreview) {
			String catalogPath = IInternalDataType.getPreviewPath(InternalDataType_Catalog.ID, catalog.getCatalogId(),
					publishPipeCode, pageIndex);
			return BackendContext.getValue() + catalogPath;
		}
		if (catalog.isStaticize()) {
			return site.getUrl(publishPipeCode) + catalog.getPath();
		} else {
			String catalogPath = IInternalDataType.getViewPath(InternalDataType_Catalog.ID, catalog.getCatalogId(),
					publishPipeCode, pageIndex);
			return BackendContext.getValue() + catalogPath;
		}
	}

	public static String getCatalogListLink(CmsSite site, CmsCatalog catalog, int pageIndex, String publishPipeCode, boolean isPreview) {
		if (catalog.getCatalogType().equals(CatalogType_Link.ID)) {
			return InternalUrlUtils.getActualUrl(catalog.getRedirectUrl(), publishPipeCode, isPreview);
		}
		if (isPreview) {
			String catalogPath = IInternalDataType.getPreviewPath(InternalDataType_Catalog.ID, catalog.getCatalogId(),
					publishPipeCode, pageIndex);
			return BackendContext.getValue() + catalogPath + "&list=Y";
		}
		if (catalog.isStaticize()) {
			String link = site.getUrl(publishPipeCode) + catalog.getPath();
			if (StringUtils.isNotEmpty(catalog.getIndexTemplate(publishPipeCode))) {
				link = link + (pageIndex > 1 ? "list_" + pageIndex : "list") + StringUtils.DOT + site.getStaticSuffix(publishPipeCode);
			}
			return link;
		} else {
			String catalogPath = IInternalDataType.getViewPath(InternalDataType_Catalog.ID, catalog.getCatalogId(),
					publishPipeCode, pageIndex);
			return BackendContext.getValue() + catalogPath + "&list=Y";
		}
	}
}
