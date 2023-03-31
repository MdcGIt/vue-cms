package com.ruoyi.cms.stat.controller.front;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.cms.stat.domain.CmsSiteVisitLog;
import com.ruoyi.cms.stat.service.ICmsStatService;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.system.security.SaAdminCheckLogin;

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
@RequestMapping("/api/stat")
public class CmsStatApiController extends BaseRestController {
	
	private final ICmsStatService cmsStatService;

	/**
	 * 网站访问统计
	 * 
	 * @param siteId
	 * @param catalogId
	 * @param contentId
	 * @return
	 */
	@GetMapping
	public void siteVisit(@RequestParam("sid") Long siteId, @RequestParam(value = "cid", required = false, defaultValue = "0") Long catalogId,
			@RequestParam(value = "cid", required = false, defaultValue = "0") Long contentId) {
		try {
			CmsSiteVisitLog log = new CmsSiteVisitLog();
			log.fill(ServletUtils.getRequest());
			log.setSiteId(siteId);
			log.setCatalogId(catalogId);
			log.setContentId(contentId);
			log.setEvtTime(LocalDateTime.now());
			this.cmsStatService.addSiteVisitLog(log);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
