package com.ruoyi.stat.service;

import java.util.List;

import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.stat.IStatType;

public interface IStatService {

	IStatType getStatType(String typeId);

	List<IStatType> getStatTypes();

	List<TreeNode<String>> getStatTreeDatas();
}
