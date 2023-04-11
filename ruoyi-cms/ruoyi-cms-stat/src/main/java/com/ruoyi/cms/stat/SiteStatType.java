package com.ruoyi.cms.stat;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.stat.IStatType;
import com.ruoyi.stat.StatMenu;

@Component
public class SiteStatType implements IStatType {

	private final static List<StatMenu> STAT_MENU = List.of(new StatMenu("CmsSiteStat", "", "{STAT.MENU.CmsSiteStat}", 1),
			new StatMenu("BdSiteTrendOverview", "CmsSiteStat", "{STAT.MENU.BdSiteTrendOverview}", 1),
			new StatMenu("BdSiteTimeTrend", "CmsSiteStat", "{STAT.MENU.BdSiteTimeTrend}", 2));

	@Override
	public List<StatMenu> getStatMenus() {
		return STAT_MENU;
	}
}
