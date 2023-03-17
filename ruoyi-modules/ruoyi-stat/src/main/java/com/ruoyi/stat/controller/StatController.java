package com.ruoyi.stat.controller;

import java.util.List;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.stat.service.IStatService;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.service.ISysI18nDictService;

import lombok.RequiredArgsConstructor;

/**
 * 统计数据
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
@RestController
@RequiredArgsConstructor
@RequestMapping("/stat")
public class StatController extends BaseRestController {
	
	private final IStatService statService;
	
	private final ISysI18nDictService i18nDictService;
	
	@GetMapping("/tree")
	public R<?> bindStatTreeData() {
		List<TreeNode<String>> treeMenus = this.statService.getStatTreeDatas();
		
		String languageTag = LocaleContextHolder.getLocale().toLanguageTag();
		treeMenus.forEach(node -> {
			node.setLabel(this.i18nDictService.getLangValue(languageTag, "StatTreeNode.name." + node.getId()));
		});
		return R.ok(TreeNode.build(treeMenus));
	}
}
