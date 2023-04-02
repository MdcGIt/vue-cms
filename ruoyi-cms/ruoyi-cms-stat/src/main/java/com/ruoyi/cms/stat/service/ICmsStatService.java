package com.ruoyi.cms.stat.service;

import com.ruoyi.cms.stat.domain.CmsSiteVisitLog;

public interface ICmsStatService {

	/**
	 * 添加站点访问统计
	 * 
	 * @param log
	 */
	void addSiteVisitLog(CmsSiteVisitLog log);
}
