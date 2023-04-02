package com.ruoyi.contentcore.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.NumberUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.SortUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileExUtils;
import com.ruoyi.contentcore.ContentCoreConsts;
import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.dto.PublishPipeProp;
import com.ruoyi.contentcore.domain.dto.SiteDTO;
import com.ruoyi.contentcore.domain.dto.SiteDefaultTemplateDTO;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.listener.event.AfterSiteDeleteEvent;
import com.ruoyi.contentcore.listener.event.AfterSiteSaveEvent;
import com.ruoyi.contentcore.mapper.CmsSiteMapper;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;
import com.ruoyi.contentcore.util.SiteUtils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl extends ServiceImpl<CmsSiteMapper, CmsSite> implements ISiteService {

	/**
	 * 缓存key前缀
	 */
	private static final String CACHE_PREFIX = "cms:site:";

	private final ApplicationContext applicationContext;

	private final RedisCache redisCache;

	@Override
	public CmsSite getSite(Long siteId) {
		CmsSite site = this.redisCache.getCacheObject(CACHE_PREFIX + siteId);
		if (Objects.isNull(site)) {
			site = this.getById(siteId);
			Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
			this.redisCache.setCacheObject(CACHE_PREFIX + siteId, site);
		}
		return site;
	}

	@Override
	public CmsSite getCurrentSite(HttpServletRequest request) {
		CmsSite site = null;
		String siteId = ServletUtils.getHeader(request, ContentCoreConsts.Header_CurrentSite);
		if (NumberUtils.isDigits(siteId)) {
			try {
				site = this.getSite(Long.valueOf(siteId));
			} catch (Exception e) {
			}
		}
		if (Objects.isNull(site)) {
			site = this.lambdaQuery().one();
		}
		Assert.notNull(site, ContentCoreErrorCode.NO_SITE::exception);
		return site;
	}

	@Override
	@Transactional
	public CmsSite addSite(SiteDTO dto) {
		boolean checkSiteUnique = this.checkSiteUnique(dto.getName(), dto.getPath(), 0L);
		Assert.isTrue(checkSiteUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("name", "path"));

		String siteRoot = SiteUtils.getSiteResourceRoot(dto.getPath());
		Assert.isFalse(new File(siteRoot).exists(),ContentCoreErrorCode.EXISTS_SITE_PATH::exception);
		// 创建站点目录
		FileExUtils.mkdirs(siteRoot);
		
		CmsSite site = new CmsSite();
		BeanUtils.copyProperties(dto, site, "siteId");
		if (StringUtils.isNotEmpty(site.getResourceUrl()) && !site.getResourceUrl().endsWith("/")) {
			site.setResourceUrl(site.getResourceUrl() + "/");
		}
		site.setSortFlag(SortUtils.getDefaultSortValue());
		site.createBy(dto.getOperator().getUsername());
		this.save(site);
		this.clearCache(site.getSiteId());
		return site;
	}

	@Override
	@Transactional
	public CmsSite saveSite(SiteDTO dto) {
		boolean checkSiteUnique = this.checkSiteUnique(dto.getName(), dto.getPath(), dto.getSiteId());
		Assert.isTrue(checkSiteUnique, () -> CommonErrorCode.DATA_CONFLICT.exception("name", "path"));

		CmsSite site = this.getById(dto.getSiteId());

		BeanUtils.copyProperties(dto, site, "path");
		if (StringUtils.isNotEmpty(site.getResourceUrl()) && !site.getResourceUrl().endsWith("/")) {
			site.setResourceUrl(site.getResourceUrl() + "/");
		}
		// 发布通道数据处理
		Map<String, Map<String, Object>> publishPipeProps = dto.getPublishPipeDatas().stream()
				.collect(Collectors.toMap(PublishPipeProp::getPipeCode, PublishPipeProp::getProps));
		site.setPublishPipeProps(publishPipeProps);
		site.updateBy(dto.getOperator().getUsername());
		this.updateById(site);
		this.clearCache(site.getSiteId());

		this.applicationContext.publishEvent(new AfterSiteSaveEvent(this, site, dto));
		return site;
	}

	/**
	 * 删除站点数据，不删除文件系统站点相关目录。一般站点目录不建议修改，必须删除时建议去文件系统直接删除。
	 */
	@Override
	@Transactional
	public void deleteSite(Long siteId) throws IOException {
		CmsSite site = this.getById(siteId);
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));

		this.removeById(site.getSiteId());
		this.clearCache(site.getSiteId());
		applicationContext.publishEvent(new AfterSiteDeleteEvent(this, site));
	}

	/**
	 * 校验站点名称/目录是否唯一
	 * 
	 * @param siteName 站点名称
	 * @param sitePath 站点目录
	 * @param siteId 站点ID
	 * @return 结果
	 */
	@Override
	public boolean checkSiteUnique(String siteName, String sitePath, Long siteId) {
		return this.lambdaQuery().and(wrapper ->
						wrapper.eq(CmsSite::getName, siteName).or().eq(CmsSite::getPath, sitePath))
				.ne(IdUtils.validate(siteId), CmsSite::getSiteId, siteId)
				.count() == 0;
	}

	@Override
	public void saveSiteDefaultTemplate(SiteDefaultTemplateDTO dto) {
		CmsSite site = this.getSite(dto.getSiteId());
		List<PublishPipeProp> publishPipeProps = dto.getPublishPipeProps();
		for (PublishPipeProp ppp : publishPipeProps) {
			Map<String, Object> sitePublishPipeProps = site.getPublishPipeProps(ppp.getPipeCode());
			sitePublishPipeProps.putAll(ppp.getProps());
		}
		site.updateBy(dto.getOperator().getUsername());
		this.updateById(site);

		this.clearCache(site.getSiteId());
	}

	@Override
	public void saveSiteExtend(Long siteId, Map<String, String> configs, String operator) {
		CmsSite site = this.getSite(siteId);
		ConfigPropertyUtils.filterConfigProps(configs, IProperty.UseType.Site);

		site.setConfigProps(configs);
		site.updateBy(operator);
		this.updateById(site);
		this.clearCache(site.getSiteId());
	}

	@Override
	public void clearCache(long siteId) {
		this.redisCache.deleteObject(CACHE_PREFIX + siteId);
	}
}
