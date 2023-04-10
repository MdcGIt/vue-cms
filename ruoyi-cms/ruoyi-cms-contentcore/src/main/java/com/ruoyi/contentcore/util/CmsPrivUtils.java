package com.ruoyi.contentcore.util;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.ruoyi.common.security.SecurityUtils;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.perms.CatalogPermissionType;
import com.ruoyi.contentcore.perms.CatalogPermissionType.CatalogPrivItem;
import com.ruoyi.contentcore.perms.SitePermissionType;
import com.ruoyi.contentcore.perms.SitePermissionType.SitePrivItem;
import com.ruoyi.system.domain.SysPermission;

import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.NotPermissionException;

/**
 * 内容核心权限工具类
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public class CmsPrivUtils {
	
	/**
	 * 合并多个位权限配置
	 * 
	 * @param permissions
	 */
	public static void mergePermission(String permissionType, List<SysPermission> permissions) {
		Map<String, BitSet> map = new HashMap<>();
		permissions.forEach(permission -> {
			String json = permission.getPermissions().get(permissionType);
			Map<String,long[]> perms = JacksonUtils.fromMap(json, long[].class);
			if(perms != null) {
				perms.entrySet().forEach(e -> {
					BitSet bitSet = map.get(e.getKey());
					if (bitSet != null) {
						bitSet.or(BitSet.valueOf(e.getValue()));
					} else {
						map.put(e.getKey(), BitSet.valueOf(e.getValue()));
					}
				});
			}
		});
	}
	
	/**
	 * 站点权限项列表转成持久化存储序列化字符串
	 * 
	 * @param permissionKeys
	 * @return
	 */
	public static String convertSitePermissionKeys(List<String> permissionKeys) {
		Map<String, BitSet> map = new HashMap<>(); // <SiteId, List<SitePrivItem>>
		permissionKeys.forEach(key -> {
			String[] split = StringUtils.split(key, ":");
			String siteId = split[1];
			BitSet bitSet = map.get(siteId);
			if (bitSet == null) {
				bitSet = new BitSet(SitePrivItem.values().length);
				map.put(siteId, bitSet);
			}
			bitSet.set(SitePrivItem.valueOf(split[0]).bitIndex());
		});
		Map<String, long[]> permsMap = new HashMap<>();
		map.entrySet().forEach(e -> {
			permsMap.put(e.getKey(), e.getValue().toLongArray());
		});
		return JacksonUtils.to(permsMap);
	}
	
	/**
	 * 解析权限配置字符串传，转成站点权限项列表
	 * 
	 * @param json
	 * @return
	 */
	public static List<String> parseSitePermissionJson(String json) {
		List<String> permissions = new ArrayList<>();
		Map<String, Long[]> sitePrivs = JacksonUtils.fromMap(json, Long[].class);
		if (sitePrivs != null) {
			sitePrivs.entrySet().forEach(e -> {
				BitSet bitSet = BitSet.valueOf(Stream.of(e.getValue()).mapToLong(Long::longValue).toArray());
				SitePrivItem[] values = SitePrivItem.values();
				for (int i = 0; i < values.length; i++) {
					if (bitSet.get(i)) {
						permissions.add(values[i].getPermissionKey(e.getKey()));
					}
				}
			});
		}
		return permissions;
	}

	/**
	 * 校验站点权限
	 * 
	 * @param siteId
	 * @param privItem
	 * @param loginUser
	 * @return
	 */
	public static void checkSitePermission(Long siteId, SitePrivItem privItem, LoginUser loginUser) {
		if (SecurityUtils.isSuperAdmin(loginUser.getUserId())) {
			return;
		}
		String permissionKey = privItem.getPermissionKey(siteId.toString());
		if (!loginUser.getPermissions().contains(permissionKey)) {
			throw new NotPermissionException(permissionKey, loginUser.getUserType()).setCode(SaErrorCode.CODE_11051);
		}
	}
	
	/**
	 * 是否拥有指定站点的指定权限
	 * 
	 * @param siteId
	 * @param privItem
	 * @param loginUser
	 * @return
	 */
	public static boolean hasSitePermission(Long siteId, SitePrivItem privItem, LoginUser loginUser) {
		if (SecurityUtils.isSuperAdmin(loginUser.getUserId())) {
			return true;
		}
		String permissionKey = privItem.getPermissionKey(siteId.toString());
		return loginUser.getPermissions().contains(permissionKey);
	}

	/**
	 * 添加指定站点的所有权限给指定权限配置
	 * 
	 * @param siteId
	 * @param userId
	 */
	public static void grantSitePermission(Long siteId, SysPermission permission) {
		List<String> list = Stream.of(SitePrivItem.values()).map(item -> item.getPermissionKey(siteId.toString())).toList();
		
		String json = permission.getPermissions().get(SitePermissionType.ID);
		List<String> permissionKeys = parseSitePermissionJson(json);
		permissionKeys.addAll(list);
		json = convertSitePermissionKeys(permissionKeys);
		permission.getPermissions().put(SitePermissionType.ID, json);
	}
	
	/**
	 * 栏目权限项列表转成持久化存储序列化字符串
	 * 
	 * @param permissionKeys
	 * @return
	 */
	public static String convertCatalogPermissionKeys(List<String> permissionKeys) {
		Map<String, BitSet> map = new HashMap<>(); // <CatalogId, List<CatalogPrivItem>>
		permissionKeys.forEach(key -> {
			String[] split = StringUtils.split(key, ":");
			String catalogId = split[1];
			BitSet bitSet = map.get(catalogId);
			if (bitSet == null) {
				bitSet = new BitSet(CatalogPrivItem.values().length);
				map.put(catalogId, bitSet);
			}
			bitSet.set(CatalogPrivItem.valueOf(split[0]).bitIndex());
		});
		Map<String, long[]> permsMap = new HashMap<>();
		map.entrySet().forEach(e -> {
			permsMap.put(e.getKey(), e.getValue().toLongArray());
		});
		return JacksonUtils.to(permsMap);
	}
	
	/**
	 * 解析权限配置字符串传，转成栏目权限项列表
	 * 
	 * @param json
	 * @return
	 */
	public static List<String> parseCatalogPermissionJson(String json) {
		List<String> permissions = new ArrayList<>();
		Map<String, Long[]> catalogPrivs = JacksonUtils.fromMap(json, Long[].class);
		if (catalogPrivs != null) {
			catalogPrivs.entrySet().forEach(e -> {
				BitSet bitSet = BitSet.valueOf(Stream.of(e.getValue()).mapToLong(Long::longValue).toArray());
				CatalogPrivItem[] values = CatalogPrivItem.values();
				for (int i = 0; i < values.length; i++) {
					if (bitSet.get(i)) {
						permissions.add(values[i].getPermissionKey(e.getKey()));
					}
				}
			});
		}
		return permissions;
	}

	/**
	 * 校验栏目权限
	 * 
	 * @param catalogId
	 * @param privItem
	 * @param loginUser
	 * @return
	 */
	public static void checkCatalogPermission(Long catalogId, CatalogPrivItem privItem, LoginUser loginUser) {
		String permissionKey = privItem.getPermissionKey(catalogId.toString());
		if (!loginUser.getPermissions().contains(permissionKey)) {
			throw new NotPermissionException(permissionKey, loginUser.getUserType()).setCode(SaErrorCode.CODE_11051);
		}
	}
	
	/**
	 * 是否拥有指定栏目的指定权限
	 * 
	 * @param catalogId
	 * @param privItem
	 * @param loginUser
	 * @return
	 */
	public static boolean hasCatalogPermission(Long catalogId, CatalogPrivItem privItem, LoginUser loginUser) {
		String permissionKey = privItem.getPermissionKey(catalogId.toString());
		return loginUser.getPermissions().contains(permissionKey);
	}

	/**
	 * 添加指定站点的所有权限给指定权限配置
	 * 
	 * @param catlaogId
	 * @param userId
	 */
	public static void grantCatalogPermission(Long catlaogId, SysPermission permission) {
		List<String> list = Stream.of(CatalogPrivItem.values()).map(item -> item.getPermissionKey(catlaogId.toString())).toList();
		
		String json = permission.getPermissions().get(CatalogPermissionType.ID);
		List<String> permissionKeys = parseCatalogPermissionJson(json);
		permissionKeys.addAll(list);
		json = convertCatalogPermissionKeys(permissionKeys);
		permission.getPermissions().put(CatalogPermissionType.ID, json);
	}
}
