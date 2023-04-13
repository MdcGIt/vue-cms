package com.ruoyi.contentcore.util;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.contentcore.perms.BitSetPrivItem;
import com.ruoyi.contentcore.perms.CatalogPermissionType;
import com.ruoyi.contentcore.perms.CatalogPermissionType.CatalogPrivItem;
import com.ruoyi.contentcore.perms.PageWidgetPermissionType;
import com.ruoyi.contentcore.perms.PageWidgetPermissionType.PageWidgetPrivItem;
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
	 * BitSet权限序列化
	 * 
	 * @param permissionMap
	 * @return
	 */
	public static String serializeBitSetPermission(Map<String, BitSet> permissionMap) {
		Map<String, long[]> permsMap = new HashMap<>();
		permissionMap.entrySet().forEach(e -> {
			permsMap.put(e.getKey(), e.getValue().toLongArray());
		});
		return JacksonUtils.to(permsMap);
	}

	/**
	 * BitSet权限反序列化
	 * 
	 * @param json
	 * @return
	 */
	public static Map<String, BitSet> deserializeBitSetPermission(String json) {
		Map<String, BitSet> permissionMap = new HashMap<>();
		Map<String, long[]> privs = JacksonUtils.fromMap(json, long[].class);
		if (privs != null) {
			privs.entrySet().forEach(e -> {
				BitSet bitSet = BitSet.valueOf(e.getValue());
				permissionMap.put(e.getKey(), bitSet);
			});
		}
		return permissionMap;
	}

	/**
	 * 合并多个权限配置
	 * 
	 * @param permissionJsonList
	 */
	public static String mergeBitSetPermissions(List<String> permissionJsonList) {
		Map<String, BitSet> map = new HashMap<>();
		permissionJsonList.forEach(json -> {
			Map<String, BitSet> bitSet = deserializeBitSetPermission(json);
			bitSet.entrySet().forEach(e -> {
				BitSet bs = map.get(e.getKey());
				if (bs == null) {
					map.put(e.getKey(), e.getValue());
				} else {
					bs.or(e.getValue());
				}
			});
		});
		return serializeBitSetPermission(map);
	}

	/**
	 * 授权
	 * 
	 * @param <T>
	 * @param key
	 * @param privItems
	 * @param permission
	 */
	public static <T> void grantBitSetPermission(String key, BitSetPrivItem[] privItems, SysPermission permission) {
		BitSet bitSet = new BitSet(privItems.length);
		for (BitSetPrivItem item : privItems) {
			bitSet.set(item.bitIndex());
		}
		String json = permission.getPermissions().get(SitePermissionType.ID);
		Map<String, BitSet> sitePrivs = deserializeBitSetPermission(json);
		sitePrivs.put(key, bitSet);
		json = serializeBitSetPermission(sitePrivs);
		permission.getPermissions().put(SitePermissionType.ID, json);
	}

	/**
	 * 校验perms是否包含指定权限
	 * 
	 * @param key
	 * @param privItem
	 * @param perms
	 * @return
	 */
	public static boolean hasBitSetPermission(String key, BitSetPrivItem privItem, Map<String, BitSet> perms) {
		BitSet bitSet = perms.get(key);
		return Objects.nonNull(bitSet) && bitSet.get(privItem.bitIndex());
	}

	/**
	 * 校验用户是否拥有指定权限
	 * 
	 * @param permissionType
	 * @param key
	 * @param privItem
	 * @param loginUser
	 * @return
	 */
	public static boolean hasBitSetPermission(String permissionType, String key, BitSetPrivItem privItem,
			LoginUser loginUser) {
		if (loginUser.isSuperAdministrator()) {
			return true;
		}
		String json = loginUser.getPermissions().get(permissionType);
		List<Long> list = JacksonUtils.getAsList(json, key, Long.class);
		if (list != null) {
			BitSet bitSet = BitSet.valueOf(list.stream().mapToLong(Long::longValue).toArray());
			if (bitSet.get(privItem.bitIndex())) {
				return true;
			}
		}
		return false;
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
		boolean result = hasBitSetPermission(SitePermissionType.ID, siteId.toString(), privItem, loginUser);
		Assert.isTrue(result,
				() -> new NotPermissionException(privItem.getPermissionKey(siteId), loginUser.getUserType())
						.setCode(SaErrorCode.CODE_11051));
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
		return hasBitSetPermission(SitePermissionType.ID, siteId.toString(), privItem, loginUser);
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
		boolean result = hasBitSetPermission(CatalogPermissionType.ID, catalogId.toString(), privItem, loginUser);
		Assert.isTrue(result,
				() -> new NotPermissionException(privItem.getPermissionKey(catalogId), loginUser.getUserType())
						.setCode(SaErrorCode.CODE_11051));
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
		return hasBitSetPermission(CatalogPermissionType.ID, catalogId.toString(), privItem, loginUser);
	}
	
	/**
	 * 校验页面部件权限
	 * 
	 * @param pageWidgetId
	 * @param privItem
	 * @param loginUser
	 * @return
	 */
	public static void checkPageWidgetPermission(Long pageWidgetId, PageWidgetPrivItem privItem, LoginUser loginUser) {
		boolean result = hasBitSetPermission(PageWidgetPermissionType.ID, pageWidgetId.toString(), privItem, loginUser);
		Assert.isTrue(result,
				() -> new NotPermissionException(privItem.getPermissionKey(pageWidgetId), loginUser.getUserType())
						.setCode(SaErrorCode.CODE_11051));
	}

	/**
	 * 是否拥有指定页面部件的指定权限
	 * 
	 * @param pageWidgetId
	 * @param privItem
	 * @param loginUser
	 * @return
	 */
	public static boolean hasPageWidgetPermission(Long pageWidgetId, PageWidgetPrivItem privItem, LoginUser loginUser) {
		return hasBitSetPermission(PageWidgetPermissionType.ID, pageWidgetId.toString(), privItem, loginUser);
	}
}
