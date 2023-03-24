package com.ruoyi.stat;

import java.util.List;

import com.ruoyi.common.domain.TreeNode;

/**
 * 统计类型
 * 
 * <p>
 * 每一个统计类型对应一种统计数据的存储及展示，前端根据统计类型进行分类展示
 * </p>
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public interface IStatType {
	
	/**
	 * 统计树
	 */
	public List<TreeNode<String>> getStatTreeNodes();
}