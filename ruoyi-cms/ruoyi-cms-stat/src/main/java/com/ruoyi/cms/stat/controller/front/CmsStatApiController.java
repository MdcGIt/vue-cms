package com.ruoyi.cms.stat.controller.front;

import com.ruoyi.cms.stat.core.CmsStat;
import com.ruoyi.cms.stat.domain.CmsSiteVisitLog;
import com.ruoyi.contentcore.domain.vo.ContentDynamicDataVO;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.validator.LongId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统计数据
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stat/visit")
public class CmsStatApiController extends BaseRestController {

	private final List<CmsStat> statList;

	/**
	 * 网站访问统计
	 *
	 * @param siteId
	 * @param catalogId
	 * @param contentId
	 * @return
	 */
	@GetMapping
	public void visitSite(
			@RequestParam("sid") Long siteId,
			@RequestParam(value = "cid", required = false, defaultValue = "0") Long catalogId,
			@RequestParam(value = "id", required = false, defaultValue = "0") Long contentId) {
		try {
			CmsSiteVisitLog log = new CmsSiteVisitLog();
			log.fill(ServletUtils.getRequest());
			log.setSiteId(siteId);
			log.setCatalogId(catalogId);
			log.setContentId(contentId);
			log.setEvtTime(LocalDateTime.now());

			this.statList.forEach(stat -> stat.deal(log));
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
