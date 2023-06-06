package com.ruoyi.contentcore.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.vo.SiteStatVO;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.service.ISiteStatService;
import com.ruoyi.system.security.SaAdminCheckLogin;

import lombok.RequiredArgsConstructor;

/**
 * 站点相关资源统计数据控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@SaAdminCheckLogin
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/dashboard")
public class SiteStatController extends BaseRestController {

	private final ISiteService siteService;
	
	private final ISiteStatService siteStatService;
	
	@GetMapping("/stat")
	public R<?> getSiteStat() {
		CmsSite site = siteService.getCurrentSite(ServletUtils.getRequest());
		SiteStatVO siteStat = siteStatService.getSiteStat(site);
		return R.ok(siteStat);
	}
}
