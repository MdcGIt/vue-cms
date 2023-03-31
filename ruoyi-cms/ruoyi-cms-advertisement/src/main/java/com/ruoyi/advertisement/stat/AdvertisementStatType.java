package com.ruoyi.advertisement.stat;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.stat.IStatType;
import com.ruoyi.stat.StatMenu;

@Component
public class AdvertisementStatType implements IStatType {

	private final static List<StatMenu> STAT_MENU = List.of(new StatMenu("Advertisement", "", "广告数据统计", 2),
			new StatMenu("AdClick", "Advertisement", "广告点击日志", 1),
			new StatMenu("AdView", "Advertisement", "广告展现日志", 2));

	@Override
	public List<StatMenu> getStatMenus() {
		return STAT_MENU;
	}
}
