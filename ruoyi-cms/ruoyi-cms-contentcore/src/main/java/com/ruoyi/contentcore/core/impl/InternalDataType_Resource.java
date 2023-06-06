package com.ruoyi.contentcore.core.impl;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.common.storage.local.LocalFileStorageType;
import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.core.InternalURL;
import com.ruoyi.contentcore.domain.CmsResource;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.properties.FileStorageArgsProperty;
import com.ruoyi.contentcore.properties.FileStorageArgsProperty.FileStorageArgs;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.SiteUtils;

import lombok.RequiredArgsConstructor;

/**
 * 内部数据类型：资源
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RequiredArgsConstructor
@Component(IInternalDataType.BEAN_NAME_PREFIX + InternalDataType_Resource.ID)
public class InternalDataType_Resource implements IInternalDataType {

	public final static String ID = "resource";

	private static final String InternalUrl_Param_SiteId = "sid"; // 内部链接参数：站点ID

	private static final String InternalUrl_Param_StorageType = "st"; // 内部链接参数：存储方式

	private final ISiteService siteService;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getLink(InternalURL internalUrl, int pageIndex, String publishPipeCode, boolean isPreview) {
		long siteId = MapUtils.getLongValue(internalUrl.getParams(), InternalUrl_Param_SiteId);
		CmsSite site = this.siteService.getSite(siteId);
		// 存储方式，非本地存储读取配置域名
		String storageType = MapUtils.getString(internalUrl.getParams(), InternalUrl_Param_StorageType,
				LocalFileStorageType.TYPE);
		if (LocalFileStorageType.TYPE.equals(storageType)) {
			return SiteUtils.getResourcePrefix(site) + internalUrl.getPath();
		}
		FileStorageArgs fileStorageArgs = FileStorageArgsProperty.getValue(site.getConfigProps());
		return fileStorageArgs.getDomain() + internalUrl.getPath();
	}

	/**
	 * 资源文件内部链接比较特殊，很多地方使用，路径不会变化且不缓存，不适合每次解析都从数据库读取资源信息，因此直接将路径放到内部链接上，后续解析仅需添加上站点资源地址前缀即可。
	 * 
	 * @param resource
	 * @return
	 */
	public static String getInternalUrl(CmsResource resource) {
		InternalURL internalURL = new InternalURL(ID, resource.getResourceId(), resource.getPath());
		internalURL.addParam(InternalUrl_Param_SiteId, resource.getSiteId().toString());
		internalURL.addParam(InternalUrl_Param_StorageType, resource.getStorageType());
		return internalURL.toIUrl();
	}
}
