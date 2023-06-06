package com.ruoyi.cms.stat.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.cms.stat.domain.CmsSiteVisitLog;
import com.ruoyi.cms.stat.mapper.CmsSiteVisitLogMapper;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.system.security.SaAdminCheckLogin;

import lombok.RequiredArgsConstructor;

/**
 * 统计数据
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@SaAdminCheckLogin
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/stat")
public class CmsStatController extends BaseRestController {

	private final ISiteService siteService;

	private final CmsSiteVisitLogMapper siteVisitLogMapper;

	@GetMapping
	public R<?> getSiteVisitLogList() {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		Page<CmsSiteVisitLog> page = new LambdaQueryChainWrapper<CmsSiteVisitLog>(this.siteVisitLogMapper)
				.eq(CmsSiteVisitLog::getSiteId, site.getSiteId()).orderByDesc(CmsSiteVisitLog::getEvtTime)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}
}
