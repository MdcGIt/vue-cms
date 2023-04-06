package com.ruoyi.cms.stat.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.cms.stat.baidu.BaiduTongjiUtils;
import com.ruoyi.cms.stat.baidu.domain.BaiduOverviewReport;
import com.ruoyi.cms.stat.baidu.domain.BaiduSite;
import com.ruoyi.cms.stat.properties.BaiduTjAccessTokenProperty;
import com.ruoyi.cms.stat.service.ICmsStatService;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.system.security.SaAdminCheckLogin;

import lombok.RequiredArgsConstructor;

/**
 * 百度统计数据
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/stat/baidu")
public class BaiduTongjiController extends BaseRestController {

	private final ISiteService siteService;

	private final ICmsStatService cmsStatService;

	@GetMapping("/refreshToken")
	public R<?> refreshAccessToken() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String accessToken = this.cmsStatService.refreshBaiduAccessToken(site);
		site.getConfigProps().put(BaiduTjAccessTokenProperty.ID, accessToken);
		this.siteService.updateById(site);
		return R.ok();
	}

	@GetMapping("/sites")
	public R<?> getSiteList() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		List<BaiduSite> siteList = BaiduTongjiUtils.getSiteList(accessToken);
		return R.ok(siteList);
	}

	/**
	 * 趋势概览数据
	 * 
	 * @param bdSiteId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	@GetMapping("/trendOverview")
	public R<?> getSiteOverviewTrendReport(@RequestParam Long bdSiteId, @RequestParam LocalDateTime startDate,
			@RequestParam LocalDateTime endDate) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		BaiduOverviewReport report = BaiduTongjiUtils.getSiteOverviewTimeTrend(accessToken, bdSiteId, startDate,
				endDate);
		return R.ok(report);
	}

	/**
	 * 区域分布概览数据
	 * 
	 * @param bdSiteId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	@GetMapping("/districtOverview")
	public R<?> getSiteOverviewDistrctReport(@RequestParam Long bdSiteId, @RequestParam LocalDateTime startDate,
			@RequestParam LocalDateTime endDate) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		BaiduOverviewReport report = BaiduTongjiUtils.getSiteOverviewDistrict(accessToken, bdSiteId, startDate,
				endDate);
		return R.ok(BaiduTongjiUtils.parseOverviewReportToTableData(report));
	}

	/**
	 * 其他概览数据
	 * 
	 * @param bdSiteId
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	@GetMapping("/otherOverview")
	public R<?> getSiteOtherOverviewDatas(@RequestParam Long bdSiteId, @RequestParam LocalDateTime startDate,
			@RequestParam LocalDateTime endDate) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		Map<String, BaiduOverviewReport> siteOverviewOthers = BaiduTongjiUtils.getSiteOverviewOthers(accessToken,
				bdSiteId, startDate, endDate);
		Map<String, List<Map<String, Object>>> datas = siteOverviewOthers.entrySet().stream().map(e -> {
			Map<String, List<Map<String, Object>>> map = Map.of(e.getKey(),
					BaiduTongjiUtils.parseOverviewReportToTableData(e.getValue()));
			return map.entrySet().iterator().next();
		}).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		return R.ok(datas);
	}
}
