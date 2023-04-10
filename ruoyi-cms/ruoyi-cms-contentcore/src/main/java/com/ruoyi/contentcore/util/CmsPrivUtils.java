package com.ruoyi.contentcore.util;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ruoyi.common.security.SecurityUtils;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.utils.JacksonUtils;
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
	public static String convertSitePermissionKeys(Map<String, BitSet> sitePermissions) {
		Map<String, long[]> permsMap = new HashMap<>();
		sitePermissions.entrySet().forEach(e -> {
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
	public static Map<String, BitSet> parseSitePermissionJson(String json) {
		Map<String, BitSet> sitePermissions = new HashMap<>();
		Map<String, long[]> catalogPrivs = JacksonUtils.fromMap(json, long[].class);
		if (catalogPrivs != null) {
			catalogPrivs.entrySet().forEach(e -> {
				BitSet bitSet = BitSet.valueOf(e.getValue());
				sitePermissions.put(e.getKey(), bitSet);
			});
		}
		return sitePermissions;
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
		String json = loginUser.getPermissions().get(SitePermissionType.ID);
		List<Long> list = JacksonUtils.getAsList(json, siteId.toString(), Long.class);
		if (list != null) {
			BitSet bitSet = BitSet.valueOf(list.stream().mapToLong(Long::longValue).toArray());
			if (bitSet.get(privItem.bitIndex())) {
				return;
			}
		}
		throw new NotPermissionException(privItem.getPermissionKey(siteId), loginUser.getUserType()).setCode(SaErrorCode.CODE_11051);
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
		String json = loginUser.getPermissions().get(SitePermissionType.ID);
		List<Long> list = JacksonUtils.getAsList(json, siteId.toString(), Long.class);
		if (list != null) {
			BitSet bitSet = BitSet.valueOf(list.stream().mapToLong(Long::longValue).toArray());
			if (bitSet.get(privItem.bitIndex())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasSitePermission(Long catalogId, SitePrivItem privItem, Map<String, BitSet> perms) {
		BitSet bitSet = perms.get(catalogId.toString());
		return Objects.nonNull(bitSet) && bitSet.get(privItem.bitIndex());
	}

	/**
	 * 添加指定站点的所有权限给指定权限配置
	 * 
	 * @param siteId
	 * @param userId
	 */
	public static void grantSitePermission(Long siteId, SysPermission permission) {
		BitSet bitSet = SitePrivItem.getBitSet();

		String json = permission.getPermissions().get(SitePermissionType.ID);
		Map<String, BitSet> sitePrivs = parseSitePermissionJson(json);
		sitePrivs.put(siteId.toString(), bitSet);
		json = convertSitePermissionKeys(sitePrivs);
		permission.getPermissions().put(SitePermissionType.ID, json);
	}
	
	/**
	 * 栏目权限项列表转成持久化存储序列化字符串
	 * 
	 * @param permissionKeys
	 * @return
	 */
	public static String convertCatalogPermissionKeys(Map<String, BitSet> catalogPermissions) {
		Map<String, long[]> permsMap = new HashMap<>();
		catalogPermissions.entrySet().forEach(e -> {
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
	public static Map<String, BitSet> parseCatalogPermissionJson(String json) {
		Map<String, BitSet> catalogPermissions = new HashMap<>();
		Map<String, long[]> catalogPrivs = JacksonUtils.fromMap(json, long[].class);
		if (catalogPrivs != null) {
			catalogPrivs.entrySet().forEach(e -> {
				BitSet bitSet = BitSet.valueOf(e.getValue());
				catalogPermissions.put(e.getKey(), bitSet);
			});
		}
		return catalogPermissions;
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
		if (SecurityUtils.isSuperAdmin(loginUser.getUserId())) {
			return;
		}
		String json = loginUser.getPermissions().get(CatalogPermissionType.ID);
		List<Long> list = JacksonUtils.getAsList(json, catalogId.toString(), Long.class);
		if (list != null) {
			BitSet bitSet = BitSet.valueOf(list.stream().mapToLong(Long::longValue).toArray());
			if (bitSet.get(privItem.bitIndex())) {
				return;
			}
		}
		throw new NotPermissionException(privItem.getPermissionKey(catalogId), loginUser.getUserType()).setCode(SaErrorCode.CODE_11051);
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
		if (SecurityUtils.isSuperAdmin(loginUser.getUserId())) {
			return true;
		}
		String json = loginUser.getPermissions().get(CatalogPermissionType.ID);
		List<Long> list = JacksonUtils.getAsList(json, catalogId.toString(), Long.class);
		if (list != null) {
			BitSet bitSet = BitSet.valueOf(list.stream().mapToLong(Long::longValue).toArray());
			if (bitSet.get(privItem.bitIndex())) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean hasCatalogPermission(Long catalogId, CatalogPrivItem privItem, Map<String, BitSet> perms) {
		BitSet bitSet = perms.get(catalogId.toString());
		return Objects.nonNull(bitSet) && bitSet.get(privItem.bitIndex());
	}

	/**
	 * 添加指定站点的所有权限给指定权限配置
	 * 
	 * @param catlaogId
	 * @param userId
	 */
	public static void grantCatalogPermission(Long catalogId, SysPermission permission) {
		BitSet bitSet = CatalogPrivItem.getBitSet();

		String json = permission.getPermissions().get(CatalogPermissionType.ID);
		Map<String, BitSet> catalogPrivs = parseCatalogPermissionJson(json);
		catalogPrivs.put(catalogId.toString(), bitSet);
		json = convertCatalogPermissionKeys(catalogPrivs);
		permission.getPermissions().put(CatalogPermissionType.ID, json);
	}
}
