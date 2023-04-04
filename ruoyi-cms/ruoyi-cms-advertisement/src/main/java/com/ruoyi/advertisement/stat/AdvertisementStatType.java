package com.ruoyi.advertisement.stat;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.stat.IStatType;
import com.ruoyi.stat.StatMenu;

@Component
public class AdvertisementStatType implements IStatType {

	private final static List<StatMenu> STAT_MENU = List.of(new StatMenu("CmsAdv", "", "广告数据统计", 2),
			new StatMenu("CmsAdStat", "CmsAdv", "广告综合统计", 1),
			new StatMenu("CmsAdClickLog", "CmsAdv", "广告点击日志", 2),
			new StatMenu("CmsAdViewLog", "CmsAdv", "广告展现日志", 3));

	@Override
	public List<StatMenu> getStatMenus() {
		return STAT_MENU;
	}
}
