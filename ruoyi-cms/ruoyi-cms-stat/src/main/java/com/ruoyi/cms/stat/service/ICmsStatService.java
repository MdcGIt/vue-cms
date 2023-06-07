package com.ruoyi.cms.stat.service;

import com.ruoyi.cms.stat.baidu.vo.BaiduSiteVO;
import com.ruoyi.common.domain.R;
import com.ruoyi.contentcore.domain.CmsSite;

import java.util.List;

public interface ICmsStatService {

	R<List<BaiduSiteVO>> getBaiduSiteList(CmsSite site);

	/**
	 * 刷新百度统计AccessToken
	 * 
	 * @param site
	 * @return
	 */
	void refreshBaiduAccessToken(CmsSite site);
}
