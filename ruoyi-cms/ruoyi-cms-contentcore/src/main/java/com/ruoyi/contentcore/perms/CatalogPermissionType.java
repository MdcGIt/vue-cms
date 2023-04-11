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
 * 栏目权限类型
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Component(IPermissionType.BEAN_PREFIX + CatalogPermissionType.ID)
public class CatalogPermissionType implements IPermissionType<Map<String, BitSet>> {

	public static final String ID = "Catalog";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "栏目权限";
	}

	@Override
	public String convert(Map<String, BitSet> permissionKeys) {
		return CmsPrivUtils.convertCatalogPermissionKeys(permissionKeys);
	}

	/**
	 * {<siteId: [long]>,...}
	 */
	@Override
	public Map<String, BitSet> parse(String json) {
		return CmsPrivUtils.parseCatalogPermissionJson(json);
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
				if (bitSet == null || !bitSet.get(CatalogPrivItem.valueOf(split[1]).bitIndex())) {
					return false;
				}
			}
			return true;
		} else {
			for (String key : permissionKeys) {
				String[] split = StringUtils.split(key, Spliter);
				BitSet bitSet = parse.get(split[2]);
				if (bitSet != null && bitSet.get(CatalogPrivItem.valueOf(split[1]).bitIndex())) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * 栏目权限项
	 */
	public enum CatalogPrivItem {

		View(0, "查看"),

		Edit(1, "编辑"),

		Delete(2, "删除"),

		Publish(3, "发布"),

		ShowHide(4, "显示/隐藏"),

		Move(5, "移动"),

		Sort(6, "排序");

		/**
		 * 权限项在bitset中的位置序号，从0开始，不可随意变更，变更后会导致原权限信息错误
		 */
		private int bitIndex;

		private String label;

		CatalogPrivItem(int bitIndex, String label) {
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
			CatalogPrivItem[] items = CatalogPrivItem.values();
			BitSet bitSet = new BitSet(items.length);
			for (CatalogPrivItem item : items) {
				bitSet.set(item.bitIndex());
			}
			return bitSet;
		}

		public String getPermissionKey(Long catalogId) {
			return ID + ":" + this.name() + ":" + catalogId;
		}
	}
}
