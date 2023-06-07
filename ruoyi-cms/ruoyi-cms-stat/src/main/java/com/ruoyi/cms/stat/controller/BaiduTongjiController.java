package com.ruoyi.cms.stat.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ruoyi.cms.stat.baidu.BaiduTongjiConfig;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.ruoyi.cms.stat.baidu.BaiduTongjiUtils;
import com.ruoyi.cms.stat.baidu.dto.BaiduTimeTrendDTO;
import com.ruoyi.cms.stat.baidu.vo.BaiduOverviewReportVO;
import com.ruoyi.cms.stat.baidu.vo.BaiduSiteVO;
import com.ruoyi.cms.stat.baidu.vo.BaiduTimeTrendVO;
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
 * @email 190785909@qq.com
 */
@SaAdminCheckLogin
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/stat/baidu")
public class BaiduTongjiController extends BaseRestController {

	private final ISiteService siteService;

	private final ICmsStatService cmsStatService;

	@PutMapping("/refreshToken")
	public R<?> refreshAccessToken() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		this.cmsStatService.refreshBaiduAccessToken(site);
		return R.ok();
	}

	@GetMapping("/sites")
	public R<?> getSiteList() {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		return this.cmsStatService.getBaiduSiteList(site);
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
		BaiduOverviewReportVO report = BaiduTongjiUtils.getSiteOverviewTimeTrend(accessToken, bdSiteId, startDate,
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
		BaiduOverviewReportVO report = BaiduTongjiUtils.getSiteOverviewDistrict(accessToken, bdSiteId, startDate,
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
		Map<String, BaiduOverviewReportVO> siteOverviewOthers = BaiduTongjiUtils.getSiteOverviewOthers(accessToken,
				bdSiteId, startDate, endDate);
		Map<String, List<Map<String, Object>>> datas = siteOverviewOthers.entrySet().stream().map(e -> {
			Map<String, List<Map<String, Object>>> map = Map.of(e.getKey(),
					BaiduTongjiUtils.parseOverviewReportToTableData(e.getValue()));
			return map.entrySet().iterator().next();
		}).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		return R.ok(datas);
	}

	@GetMapping("/timeTrend")
	public R<?> getSiteTimeTrend(@Validated BaiduTimeTrendDTO dto) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		String accessToken = BaiduTjAccessTokenProperty.getValue(site.getConfigProps());
		if (StringUtils.isBlank(accessToken)) {
			return R.ok();
		}
		if (StringUtils.isEmpty(dto.getGran())) {
			dto.setGran("day");
		}
		dto.setMetrics(List.of("pv_count", "visitor_count", "ip_count", "visit_count"));
		BaiduTimeTrendVO siteTimeTrend = BaiduTongjiUtils.getSiteTimeTrend(accessToken, dto);
		return R.ok(siteTimeTrend);
	}
}

