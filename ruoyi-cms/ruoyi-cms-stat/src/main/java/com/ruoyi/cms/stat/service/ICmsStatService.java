package com.ruoyi.cms.stat.service;

import com.ruoyi.contentcore.domain.CmsSite;

public interface ICmsStatService {

	/**
	 * 刷新百度统计AccessToken
	 * 
	 * @param site
	 * @return
	 */
	String refreshBaiduAccessToken(CmsSite site);
}
