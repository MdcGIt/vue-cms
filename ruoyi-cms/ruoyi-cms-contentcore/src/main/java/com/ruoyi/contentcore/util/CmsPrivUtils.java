package com.ruoyi.contentcore.util;

import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.contentcore.perms.BitSetPrivItem;
import com.ruoyi.system.domain.SysPermission;

import java.util.*;

/**
 * 内容核心权限工具类
 *
 * @author 兮玥
 * @email 190785909@qq.com
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
    public static <T> void grantBitSetPermission(String permissionType, String key, BitSetPrivItem[] privItems, SysPermission permission) {
        BitSet bitSet = new BitSet(privItems.length);
        for (BitSetPrivItem item : privItems) {
            bitSet.set(item.bitIndex());
        }
        String json = permission.getPermissions().get(permissionType);
        Map<String, BitSet> privs = deserializeBitSetPermission(json);
        privs.put(key, bitSet);
        json = serializeBitSetPermission(privs);
        permission.getPermissions().put(permissionType, json);
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
     * @param permissionJson 权限持久化json字符串
     * @param key
     * @param privItem
     * @param loginUser
     * @return
     */
    public static boolean hasBitSetPermission(String permissionJson, String key, BitSetPrivItem privItem,
                                              LoginUser loginUser) {
        if (loginUser.isSuperAdministrator()) {
            return true;
        }
        List<Long> list = JacksonUtils.getAsList(permissionJson, key, Long.class);
        if (list != null) {
            BitSet bitSet = BitSet.valueOf(list.stream().mapToLong(Long::longValue).toArray());
            if (bitSet.get(privItem.bitIndex())) {
                return true;
            }
        }
        return false;
    }
}
