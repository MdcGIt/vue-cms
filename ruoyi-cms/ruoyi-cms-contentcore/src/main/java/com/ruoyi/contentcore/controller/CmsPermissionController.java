package com.ruoyi.contentcore.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.domain.CmsCatalog;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.domain.vo.CatalogPrivVO;
import com.ruoyi.contentcore.domain.vo.SitePrivVO;
import com.ruoyi.contentcore.perms.CatalogPermissionType;
import com.ruoyi.contentcore.perms.CatalogPermissionType.CatalogPrivItem;
import com.ruoyi.contentcore.perms.SitePermissionType;
import com.ruoyi.contentcore.perms.SitePermissionType.SitePrivItem;
import com.ruoyi.contentcore.service.ICatalogService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.system.domain.SysPermission;
import com.ruoyi.system.service.ISysPermissionService;

import lombok.RequiredArgsConstructor;

/**
 * 站点/栏目/内容权限
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/perms")
public class CmsPermissionController extends BaseRestController {

	private final ISiteService siteService;

	private final ICatalogService catalogService;

	private final ISysPermissionService permissionService;

	private final SitePermissionType sitePermissionType;

	private final CatalogPermissionType catalogPermissionType;

	@GetMapping("/site")
	public R<?> getSitePermissions(@RequestParam String ownerType, @RequestParam String owner) {
		List<CmsSite> sites = this.siteService.lambdaQuery().list();

		SysPermission permission = this.permissionService.getPermissions(ownerType, owner);
		List<String> perms;
		if (Objects.nonNull(permission)) {
			String json = permission.getPermissions().get(SitePermissionType.ID);
			perms = this.sitePermissionType.parse(json);
		} else {
			perms = List.of();
		}

		List<SitePrivVO> sitePrivs = sites.stream().map(site -> {
			SitePrivVO vo = new SitePrivVO();
			vo.setSiteId(site.getSiteId());
			vo.setName(site.getName());
			SitePrivItem[] values = SitePrivItem.values();
			for (SitePrivItem privItem : values) {
				String permKey = privItem.getPermissionKey(site.getSiteId().toString());
				vo.getPerms().put(privItem.name(), perms.contains(permKey));
			}
			return vo;
		}).toList();

		List<Map<String, String>> siteSubPrivs = Stream.of(SitePrivItem.values())
				.map(ssp -> Map.of("id", ssp.name(), "name", ssp.label())).toList();
		return R.ok(Map.of("sitePrivs", sitePrivs, "sitePrivItems", siteSubPrivs));
	}

	@GetMapping("/catalog")
	public R<?> getCatalogPermissions(@RequestParam String ownerType, @RequestParam String owner,
			@RequestParam(required = false) Long siteId) {
		CmsSite site = IdUtils.validate(siteId) ? this.siteService.getSite(siteId)
				: this.siteService.getCurrentSite(ServletUtils.getRequest());
		Assert.notNull(site, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("siteId", siteId));
		
		List<CmsCatalog> catalogs = this.catalogService.lambdaQuery().eq(CmsCatalog::getSiteId, site.getSiteId())
				.list();

		SysPermission permission = this.permissionService.getPermissions(ownerType, owner);
		List<String> perms;
		if (Objects.nonNull(permission)) {
			String json = permission.getPermissions().get(CatalogPermissionType.ID);
			perms = this.catalogPermissionType.parse(json);
		} else {
			perms = List.of();
		}

		List<CatalogPrivVO> catalogPrivs = catalogs.stream().map(catalog -> {
			CatalogPrivVO vo = new CatalogPrivVO();
			vo.setCatalogId(catalog.getCatalogId());
			vo.setParentId(catalog.getParentId());
			vo.setName(catalog.getName());
			CatalogPrivItem[] values = CatalogPrivItem.values();
			for (CatalogPrivItem privItem : values) {
				String permKey = privItem.getPermissionKey(catalog.getCatalogId().toString());
				vo.getPerms().put(privItem.name(), perms.contains(permKey));
			}
			return vo;
		}).toList();
		List<CatalogPrivVO> treeTable = buildTreeTable(catalogPrivs);
		List<Map<String, String>> siteSubPrivs = Stream.of(CatalogPrivItem.values())
				.map(ssp -> Map.of("id", ssp.name(), "name", ssp.label())).toList();
		return R.ok(Map.of("siteId", site.getSiteId(), "catalogPrivs", treeTable, "catalogPrivItems", siteSubPrivs));
	}

	public List<CatalogPrivVO> buildTreeTable(List<CatalogPrivVO> list) {
		Map<Long, List<CatalogPrivVO>> mapChildren = list.stream().filter(n -> n.getParentId() > 0)
				.collect(Collectors.groupingBy(CatalogPrivVO::getParentId));
		List<CatalogPrivVO> result = new ArrayList<>();
		list.forEach(n -> {
			n.setChildren(mapChildren.get(n.getCatalogId()));
			if (n.getParentId() == 0) {
				result.add(n);
			}
		});
		return result;
	}
}
