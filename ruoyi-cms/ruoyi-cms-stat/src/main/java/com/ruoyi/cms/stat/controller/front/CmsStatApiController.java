package com.ruoyi.cms.stat.controller.front;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.cms.stat.domain.CmsSiteVisitLog;
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
@RequestMapping("/api/site/visit")
public class CmsStatApiController extends BaseRestController {
	
	/**
	 * 网站访问统计
	 * 
	 * @param siteId
	 * @param catalogId
	 * @param contentId
	 * @return
	 */
	@GetMapping
	public void visitSite(@RequestParam("sid") Long siteId, @RequestParam(value = "cid", required = false, defaultValue = "0") Long catalogId,
			@RequestParam(value = "id", required = false, defaultValue = "0") Long contentId) {
		try {
			CmsSiteVisitLog log = new CmsSiteVisitLog();
			log.fill(ServletUtils.getRequest());
			log.setSiteId(siteId);
			log.setCatalogId(catalogId);
			log.setContentId(contentId);
			log.setEvtTime(LocalDateTime.now());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
