package com.ruoyi.cms.stat.service.impl;

import org.springframework.stereotype.Service;

import com.ruoyi.cms.stat.baidu.BaiduTongjiUtils;
import com.ruoyi.cms.stat.exception.CmsStatErrorCode;
import com.ruoyi.cms.stat.properties.BaiduTjApiKeyProperty;
import com.ruoyi.cms.stat.properties.BaiduTjRefreshTokenProperty;
import com.ruoyi.cms.stat.properties.BaiduTjSecretKeyProperty;
import com.ruoyi.cms.stat.service.ICmsStatService;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CmsStatServiceImpl implements ICmsStatService {

	/**
	 * 刷新百度统计AccessToken
	 * 
	 * @param site
	 * @return
	 */
	@Override
	public String refreshBaiduAccessToken(CmsSite site) {
		String apiKey = BaiduTjApiKeyProperty.getValue(site.getConfigProps());
		String secretKey = BaiduTjSecretKeyProperty.getValue(site.getConfigProps());
		String refreshToken = BaiduTjRefreshTokenProperty.getValue(site.getConfigProps());

		Assert.isFalse(StringUtils.isAnyEmpty(apiKey, secretKey, refreshToken),
				CmsStatErrorCode.BAIDU_TONGJI_CONFIG_EMPTY::exception);
		return BaiduTongjiUtils.refreshAccessToken(apiKey, secretKey, refreshToken);
	}
}
