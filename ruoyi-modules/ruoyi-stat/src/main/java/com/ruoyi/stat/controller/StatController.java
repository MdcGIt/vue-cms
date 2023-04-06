package com.ruoyi.stat.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.domain.TreeNode;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.stat.service.IStatService;
import com.ruoyi.stat.user.preference.StatIndexPreference;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;

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
	
	@GetMapping("/menu/tree")
	public R<?> bindStatTreeData() {
		List<TreeNode<String>> treeMenus = this.statService.getStatMenuTree();
		SysUser user = (SysUser) StpAdminUtil.getLoginUser().getUser();
		return R.ok(Map.of("treeData", treeMenus, "defaultMenu", StatIndexPreference.getValue(user.getPreferences())));
	}
}
