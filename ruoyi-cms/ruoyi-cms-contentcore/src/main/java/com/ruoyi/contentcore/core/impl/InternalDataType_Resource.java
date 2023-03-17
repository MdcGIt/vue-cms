package com.ruoyi.contentcore.core.impl;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.core.InternalURL;
import com.ruoyi.contentcore.domain.CmsResource;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.SiteUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component(IInternalDataType.BEAN_NAME_PREFIX + InternalDataType_Resource.ID)
public class InternalDataType_Resource implements IInternalDataType {

	public final static String ID = "resource";

	private final ISiteService siteService;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getLink(InternalURL internalUrl, int pageIndex, String publishPipeCode, boolean isPreview) {
		long siteId = MapUtils.getLongValue(internalUrl.getParams(), "sid");
		CmsSite site = this.siteService.getSite(siteId);
		return SiteUtils.getResourcePrefix(site) + internalUrl.getPath();
	}

	/**
	 * 资源文件内部链接比较特殊，很多地方使用，路径不会变化且不缓存，不适合每次解析都从数据库读取资源信息，因此直接将路径放到内部链接上，后续解析仅需添加上站点资源地址前缀即可。
	 * 
	 * @param resource
	 * @return
	 */
	public static String getInternalUrl(CmsResource resource) {
		return new InternalURL(ID, resource.getResourceId(), resource.getPath(),
				Map.of("sid", resource.getSiteId().toString())).toIUrl();
	}
}
