package com.ruoyi.advertisement.controller;

import java.util.Date;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.advertisement.domain.CmsAdClickLog;
import com.ruoyi.advertisement.domain.CmsAdViewLog;
import com.ruoyi.advertisement.mapper.CmsAdClickLogMapper;
import com.ruoyi.advertisement.mapper.CmsAdViewLogMapper;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.system.security.SaAdminCheckLogin;

import lombok.RequiredArgsConstructor;

@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/ad/stat")
public class AdLogController extends BaseRestController {

	private final ISiteService siteService;
	
	private final CmsAdClickLogMapper adClickLogMapper;
	
	private final CmsAdViewLogMapper adViewLogMapper;

	@GetMapping("/click")
	public R<?> listAdClickLogs(@RequestParam("startTime") Date startTime, @RequestParam("endTime") Date endTime) {
		PageRequest pr = getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		LambdaQueryWrapper<CmsAdClickLog> q = new LambdaQueryWrapper<CmsAdClickLog>()
				.eq(CmsAdClickLog::getSiteId, site.getSiteId())
				.orderByDesc(CmsAdClickLog::getLogId);
		Page<CmsAdClickLog> page = adClickLogMapper.selectPage(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}

	@GetMapping("/view")
	public R<?> listAdViewLogs(@RequestParam("startTime") Date startTime, @RequestParam("endTime") Date endTime) {
		PageRequest pr = getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		LambdaQueryWrapper<CmsAdViewLog> q = new LambdaQueryWrapper<CmsAdViewLog>()
				.eq(CmsAdViewLog::getSiteId, site.getSiteId())
				.orderByDesc(CmsAdViewLog::getLogId);
		Page<CmsAdViewLog> page = adViewLogMapper.selectPage(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}
}
