package com.ruoyi.stat.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.springframework.stereotype.Service;

import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.stat.IStatType;
import com.ruoyi.stat.StatMenu;
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
	public List<TreeNode<String>> getStatMenuTree() {
		List<StatMenu> menus = new ArrayList<>();
		this.statTypeMap.values().stream().forEach(st -> menus.addAll(st.getStatMenus()));
		menus.sort((m1, m2) -> m1.sort() - m2.sort());
		List<TreeNode<String>> list = menus.stream().map(m -> {
			return new TreeNode<>(m.menuId(), m.parentId(), I18nUtils.get(m.name()), StringUtils.isEmpty(m.parentId()));
		}).toList();
		return TreeNode.build(list);
	}
}
