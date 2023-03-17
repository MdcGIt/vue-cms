package com.ruoyi.advertisement.stat;

import java.util.List;

import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.stat.IStatType;

public class AdvertisementStatType implements IStatType {

	private final static List<TreeNode<String>> TREE_NODES = List.of(new TreeNode<String>("Advertisement", "", "广告数据统计", true),
			new TreeNode<String>("AdClickLog", "Advertisement", "广告点击日志", false),
			new TreeNode<String>("AdViewLog", "Advertisement", "广告展现日志", false));

	@Override
	public List<TreeNode<String>> getStatTreeNodes() {
		return TREE_NODES;
	}
}
