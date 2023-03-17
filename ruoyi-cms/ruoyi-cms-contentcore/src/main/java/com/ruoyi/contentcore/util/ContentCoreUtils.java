package com.ruoyi.contentcore.util;

import java.util.Map;

import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.contentcore.core.ICatalogType;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.core.IPageWidgetType;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.template.ITemplateType;

public class ContentCoreUtils {

	/**
	 * 栏目类型
	 */
	private static final Map<String, ICatalogType> CatalogTypes = SpringUtils.getBeanMap(ICatalogType.class);

	/**
	 * 内容类型
	 */
	private static final Map<String, IContentType> ContentTypes = SpringUtils.getBeanMap(IContentType.class);

	/**
	 * 内部内容类型
	 */
	private static final Map<String, IInternalDataType> InternalDataTypes = SpringUtils
			.getBeanMap(IInternalDataType.class);

	/**
	 * 模板类型
	 */
	private static final Map<String, ITemplateType> TemplateTypes = SpringUtils.getBeanMap(ITemplateType.class);

	/**
	 * 页面组件类型
	 */
	private static final Map<String, IPageWidgetType> PageWidgetTypes = SpringUtils.getBeanMap(IPageWidgetType.class);

	public static IPageWidgetType getPageWidgetType(String typeId) {
		IPageWidgetType pwt = PageWidgetTypes.get(typeId);
		Assert.notNull(pwt, () -> ContentCoreErrorCode.UNSUPPORT_PAGE_WIDGET_TYPE.exception(typeId));
		return pwt;
	}

	public static ITemplateType getTemplateType(String typeId) {
		return TemplateTypes.get(typeId);
	}

	public static ICatalogType getCatalogType(String catalogTypeId) {
		ICatalogType ct = CatalogTypes.get(ICatalogType.BEAN_NAME_PREFIX + catalogTypeId);
		Assert.notNull(ct, () -> ContentCoreErrorCode.UNSUPPORT_CATALOG_TYPE.exception(catalogTypeId));
		return ct;
	}

	public static Map<String, ICatalogType> getCatalogTypes() {
		return CatalogTypes;
	}

	public static IContentType getContentType(String contentTypeId) {
		IContentType ct = ContentTypes.get(IContentType.BEAN_NAME_PREFIX + contentTypeId);
		Assert.notNull(ct, () -> ContentCoreErrorCode.UNSUPPORT_CONTENT_TYPE.exception(contentTypeId));
		return ct;
	}

	public static Map<String, IContentType> getContentTypes() {
		return ContentTypes;
	}

	public static IInternalDataType getInternalDataType(String typeId) {
		IInternalDataType idt = InternalDataTypes.get(IInternalDataType.BEAN_NAME_PREFIX + typeId);
		Assert.notNull(idt, () -> ContentCoreErrorCode.UNSUPPORT_INTERNAL_DATA_TYPE.exception());
		return idt;
	}

	public static Map<String, IInternalDataType> getInternalDataTypes() {
		return InternalDataTypes;
	}
}
