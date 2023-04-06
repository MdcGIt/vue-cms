package com.ruoyi.cms.stat;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.stat.IStatType;
import com.ruoyi.stat.StatMenu;

@Component
public class SiteStatType implements IStatType {

	private final static List<StatMenu> STAT_MENU = List.of(new StatMenu("CmsSiteStat", "", "网站访问统计", 1),
			new StatMenu("BdSiteTrendOverview", "CmsSiteStat", "网站概况", 1));

	@Override
	public List<StatMenu> getStatMenus() {
		return STAT_MENU;
	}
}
