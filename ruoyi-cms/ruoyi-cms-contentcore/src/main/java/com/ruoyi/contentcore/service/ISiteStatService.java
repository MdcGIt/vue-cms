package com.ruoyi.contentcore.service;

import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.vo.SiteStatVO;

public interface ISiteStatService {

	/**
	 * 获取站点相关资源统计数据
	 * 
	 * @param site
	 * @param day
	 * @return
	 */
	SiteStatVO getSiteStat(CmsSite site);
}
