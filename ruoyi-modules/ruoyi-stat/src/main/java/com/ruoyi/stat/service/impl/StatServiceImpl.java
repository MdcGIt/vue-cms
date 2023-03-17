package com.ruoyi.stat.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.stat.IStatType;
import com.ruoyi.stat.errorcode.StatErrorCode;
import com.ruoyi.stat.service.IStatService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements IStatService {

	private final Map<String, IStatType> statTypeMap;

	@Override
	public IStatType getStatType(String typeId) {
		IStatType statType = this.statTypeMap.get(typeId);
		Assert.notNull(statType, () -> StatErrorCode.UNSUPPORT_STAT_TYPE.exception(typeId));
		return statType;
	}

	@Override
	public List<IStatType> getStatTypes() {
		return this.statTypeMap.values().stream().toList();
	}

	@Override
	public List<TreeNode<String>> getStatTreeDatas() {
		List<TreeNode<String>> tree = new ArrayList<>();
		this.statTypeMap.values().forEach(st -> tree.addAll(st.getStatTreeNodes()));
		return tree;
	}
}
