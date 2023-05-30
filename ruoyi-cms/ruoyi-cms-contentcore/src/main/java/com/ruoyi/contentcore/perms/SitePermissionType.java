package com.ruoyi.contentcore.perms;

import java.util.*;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.util.CmsPrivUtils;
import com.ruoyi.system.permission.IPermissionType;

import cn.dev33.satoken.annotation.SaMode;

/**
 * 站点权限类型
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Component(IPermissionType.BEAN_PREFIX + SitePermissionType.ID)
public class SitePermissionType implements IPermissionType<Map<String, BitSet>> {

	public static final String ID = "Site";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "站点权限";
	}

	@Override
	public String serialize(Map<String, BitSet> permissionKeys) {
		return CmsPrivUtils.serializeBitSetPermission(permissionKeys);
	}

	/**
	 * {<siteId: [long]>,...}
	 */
	@Override
	public Map<String, BitSet> deserialize(String json) {
		return CmsPrivUtils.deserializeBitSetPermission(json);
	}
	
	@Override
	public String merge(List<String> permissionJsonList) {
		return CmsPrivUtils.mergeBitSetPermissions(permissionJsonList);
	}

	@Override
	public Set<String> convert(String json) {
		Set<String> set = new HashSet<>();
		SitePrivItem[] values = SitePrivItem.values();
		CmsPrivUtils.deserializeBitSetPermission(json).forEach((siteId, bitSet) -> {
			for (SitePrivItem item : values) {
				if (bitSet.get(item.bitIndex())) {
					set.add("Site" + Spliter + item.name() + Spliter + siteId);
				}
			}
		});
		return set;
	}

	@Override
	public boolean hasPermission(List<String> permissionKeys, String json, SaMode mode) {
		Map<String,BitSet> parse = deserialize(json);
		if (mode == SaMode.AND) {
			for (String key : permissionKeys) {
				String[] split = StringUtils.split(key, Spliter);
				BitSet bitSet = parse.get(split[2]);
				if (bitSet == null || !bitSet.get(SitePrivItem.valueOf(split[1]).bitIndex())) {
					return false;
				}
			}
			return true;
		} else {
			for (String key : permissionKeys) {
				String[] split = StringUtils.split(key, Spliter);
				BitSet bitSet = parse.get(split[2]);
				if (bitSet != null && bitSet.get(SitePrivItem.valueOf(split[1]).bitIndex())) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * 站点权限项
	 */
	public enum SitePrivItem implements BitSetPrivItem {

		View(0, "查看"),

		Edit(1, "编辑"),

		Delete(2, "删除"),

		Publish(3, "发布");

		/**
		 * 权限项在bitset中的位置序号，从0开始，不可随意变更，变更后会导致原权限信息错误
		 */
		private int bitIndex;

		private String label;

		SitePrivItem(int bitIndex, String label) {
			this.bitIndex = bitIndex;
			this.label = label;
		}

		@Override
		public int bitIndex() {
			return this.bitIndex;
		}

		public String label() {
			return this.label;
		}

		public String getPermissionKey(Long siteId) {
			return ID + Spliter + this.name() + Spliter + siteId;
		}
	}
}
