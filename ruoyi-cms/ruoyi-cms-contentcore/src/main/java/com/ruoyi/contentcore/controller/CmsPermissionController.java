package com.ruoyi.contentcore.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.perms.SitePermissionType;
import com.ruoyi.contentcore.perms.SitePermissionType.SiteSubPriv;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.system.domain.SysPermission;
import com.ruoyi.system.service.ISysPermissionService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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

	private final ISysPermissionService permissionService;

	private final SitePermissionType sitePermissionType;

	@Getter
	@Setter
	static class SitePrivVO {

		private Long siteId;

		private String name;

		private Map<String, Boolean> perms = new HashMap<>();
	}

	@GetMapping("/site")
	public R<?> getSitePermissions(@RequestParam String ownerType, @RequestParam String owner) {
		List<CmsSite> sites = this.siteService.lambdaQuery().list();

		SysPermission permission = this.permissionService.getPermissions(ownerType, owner);
		List<String> perms;
		if (Objects.nonNull(permission)) {
			String json = permission.getPermissions().get(SitePermissionType.ID);
			perms = this.sitePermissionType.parsePermissionKeys(json);
		} else {
			perms = List.of();
		}

		List<SitePrivVO> sitePrivs = sites.stream().map(site -> {
			SitePrivVO vo = new SitePrivVO();
			vo.setSiteId(site.getSiteId());
			vo.setName(site.getName());
			SiteSubPriv[] values = SiteSubPriv.values();
			for (SiteSubPriv subPriv : values) {
				String permKey = subPriv.getPermissionKey(site.getSiteId().toString());
				vo.getPerms().put(subPriv.name(), perms.contains(permKey));
			}
			return vo;
		}).toList();

		List<Map<String, String>> siteSubPrivs = Stream.of(SiteSubPriv.values())
				.map(ssp -> Map.of("id", ssp.name(), "name", ssp.label())).toList();
		return R.ok(Map.of("sitePrivs", sitePrivs, "siteSubPrivs", siteSubPrivs));
	}
}
