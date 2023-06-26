package com.ruoyi.contentcore.controller;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
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
import com.ruoyi.contentcore.util.CmsPrivUtils;
import com.ruoyi.system.domain.SysPermission;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysPermissionService;
import com.ruoyi.system.validator.LongId;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 站点/栏目/内容权限
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE)
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
		Map<String, BitSet> perms;
		if (Objects.nonNull(permission)) {
			String json = permission.getPermissions().get(SitePermissionType.ID);
			perms = this.sitePermissionType.deserialize(json);
		} else {
			perms = Map.of();
		}

		List<SitePrivVO> sitePrivs = sites.stream().map(site -> {
			SitePrivVO vo = new SitePrivVO();
			vo.setSiteId(site.getSiteId());
			vo.setName(site.getName());
			SitePrivItem[] values = SitePrivItem.values();
			for (SitePrivItem privItem : values) {
				vo.getPerms().put(privItem.name(),
						CmsPrivUtils.hasBitSetPermission(site.getSiteId().toString(), privItem, perms));
			}
			return vo;
		}).toList();

		List<Map<String, String>> siteSubPrivs = Stream.of(SitePrivItem.values())
				.map(ssp -> Map.of("id", ssp.name(), "name", ssp.label())).toList();
		return R.ok(Map.of("sitePrivs", sitePrivs, "sitePrivItems", siteSubPrivs));
	}

	@Getter
	@Setter
	static class SaveSitePermissionDTO {

		@NotEmpty
		private String ownerType;

		@NotEmpty
		private String owner;

		private List<SitePrivVO> perms;
	}

	@PutMapping("/site")
	public R<?> saveSitePermissions(@RequestBody @Validated SaveSitePermissionDTO dto) {
		Map<String, BitSet> map = new HashMap<>();
		dto.getPerms().forEach(vo -> {
			Long siteId = vo.getSiteId();
			BitSet bs = new BitSet(SitePrivItem.values().length);
			vo.getPerms().entrySet().forEach(e -> {
				if (e.getValue()) {
					SitePrivItem sitePrivItem = SitePrivItem.valueOf(e.getKey());
					bs.set(sitePrivItem.bitIndex());
				}
			});
			if (!bs.isEmpty()) {
				map.put(siteId.toString(), bs);
			} else {
				map.remove(siteId.toString());
			}
		});
		SysPermission permissions = this.permissionService.getPermissions(dto.getOwnerType(), dto.getOwner());
		if (permissions == null) {
			permissions = new SysPermission();
			permissions.setPermId(IdUtils.getSnowflakeId());
			permissions.setOwnerType(dto.getOwnerType());
			permissions.setOwner(dto.getOwner());
			permissions.createBy(StpAdminUtil.getLoginUser().getUsername());
		}
		permissions.getPermissions().put(SitePermissionType.ID, this.sitePermissionType.serialize(map));
		this.permissionService.saveOrUpdate(permissions);
		return R.ok();
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
		Map<String, BitSet> perms;
		if (Objects.nonNull(permission)) {
			String json = permission.getPermissions().get(CatalogPermissionType.ID);
			perms = this.catalogPermissionType.deserialize(json);
		} else {
			perms = Map.of();
		}

		List<CatalogPrivVO> catalogPrivs = catalogs.stream().map(catalog -> {
			CatalogPrivVO vo = new CatalogPrivVO();
			vo.setCatalogId(catalog.getCatalogId());
			vo.setParentId(catalog.getParentId());
			vo.setName(catalog.getName());
			CatalogPrivItem[] values = CatalogPrivItem.values();
			for (CatalogPrivItem privItem : values) {
				vo.getPerms().put(privItem.name(),
						CmsPrivUtils.hasBitSetPermission(catalog.getCatalogId().toString(), privItem, perms));
			}
			return vo;
		}).toList();
		List<CatalogPrivVO> treeTable = buildTreeTable(catalogPrivs);
		List<Map<String, String>> siteSubPrivs = Stream.of(CatalogPrivItem.values())
				.map(ssp -> Map.of("id", ssp.name(), "name", ssp.label())).toList();
		return R.ok(Map.of("siteId", site.getSiteId(), "catalogPrivs", treeTable, "catalogPrivItems", siteSubPrivs));
	}

	private List<CatalogPrivVO> buildTreeTable(List<CatalogPrivVO> list) {
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

	@Getter
	@Setter
	static class SaveCatalogPermissionDTO {

		@NotEmpty
		private String ownerType;

		@NotEmpty
		private String owner;

		@LongId
		private Long siteId;

		private List<CatalogPrivVO> perms;
	}

	@PutMapping("/catalog")
	public R<?> saveCatalogPermissions(@RequestBody @Validated SaveCatalogPermissionDTO dto) {
		Map<String, BitSet> map;
		SysPermission permissions = this.permissionService.getPermissions(dto.getOwnerType(), dto.getOwner());
		if (permissions == null) {
			permissions = new SysPermission();
			permissions.setPermId(IdUtils.getSnowflakeId());
			permissions.setOwnerType(dto.getOwnerType());
			permissions.setOwner(dto.getOwner());
			permissions.createBy(StpAdminUtil.getLoginUser().getUsername());
		}
		String json = permissions.getPermissions().get(CatalogPermissionType.ID);
		if (StringUtils.isNotEmpty(json)) {
			map = this.catalogPermissionType.deserialize(json);
		} else {
			map = new HashMap<>();
		}
		this.invokeCatalogPerms(dto.getPerms(), map);
		permissions.getPermissions().put(CatalogPermissionType.ID, this.catalogPermissionType.serialize(map));
		this.permissionService.saveOrUpdate(permissions);
		return R.ok();
	}

	private void invokeCatalogPerms(List<CatalogPrivVO> list, Map<String, BitSet> map) {
		for (CatalogPrivVO vo : list) {
			Long catalogId = vo.getCatalogId();
			BitSet bs = new BitSet(CatalogPrivItem.values().length);
			vo.getPerms().entrySet().forEach(e -> {
				if (e.getValue()) {
					CatalogPrivItem catalogPrivItem = CatalogPrivItem.valueOf(e.getKey());
					bs.set(catalogPrivItem.bitIndex());
				}
			});
			if (!bs.isEmpty()) {
				map.put(catalogId.toString(), bs);
			} else {
				map.remove(catalogId.toString());
			}
			if (StringUtils.isNotEmpty(vo.getChildren())) {
				this.invokeCatalogPerms(vo.getChildren(), map);
			}
		}
	}
}
