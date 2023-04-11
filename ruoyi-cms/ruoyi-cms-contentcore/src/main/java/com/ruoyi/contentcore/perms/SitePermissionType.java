package com.ruoyi.contentcore.perms;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public String convert(Map<String, BitSet> permissionKeys) {
		return CmsPrivUtils.convertSitePermissionKeys(permissionKeys);
	}

	/**
	 * {<siteId: [long]>,...}
	 */
	@Override
	public Map<String, BitSet> parse(String json) {
		return CmsPrivUtils.parseSitePermissionJson(json);
	}
	
	@Override
	public String merge(List<String> permissionJsonList) {
		Map<String, BitSet> map = new HashMap<>();
		permissionJsonList.forEach(json -> {
			Map<String, BitSet> bitSet = parse(json);
			bitSet.entrySet().forEach(e -> {
				BitSet bs = map.get(e.getKey());
				if (bs == null) {
					map.put(e.getKey(), e.getValue());
				} else {
					bs.or(e.getValue());
				}
			});
		});
		return convert(map);
	}
	
	@Override
	public boolean hasPermission(List<String> permissionKeys, String json, SaMode mode) {
		Map<String,BitSet> parse = parse(json);
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
	public enum SitePrivItem {

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

		public int bitIndex() {
			return this.bitIndex;
		}

		public String label() {
			return this.label;
		}
		
		public static BitSet getBitSet() {
			SitePrivItem[] items = SitePrivItem.values();
			BitSet bitSet = new BitSet(items.length);
			for (SitePrivItem item : items) {
				bitSet.set(item.bitIndex());
			}
			return bitSet;
		}

		public String getPermissionKey(Long siteId) {
			return ID + Spliter + this.name() + Spliter + siteId;
		}
	}
}
