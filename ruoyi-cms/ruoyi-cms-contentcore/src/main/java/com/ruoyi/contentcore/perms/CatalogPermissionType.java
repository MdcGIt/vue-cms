package com.ruoyi.contentcore.perms;

import java.util.*;

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
	public String serialize(Map<String, BitSet> permissionKeys) {
		return CmsPrivUtils.serializeBitSetPermission(permissionKeys);
	}

	/**
	 * {<catalogId: [long]>,...}
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
		CatalogPrivItem[] values = CatalogPrivItem.values();
		CmsPrivUtils.deserializeBitSetPermission(json).forEach((catalogId, bitSet) -> {
			for (CatalogPrivItem item : values) {
				if (bitSet.get(item.bitIndex())) {
					set.add("Catalog" + Spliter + item.name() + Spliter + catalogId);
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
	public enum CatalogPrivItem implements BitSetPrivItem {

		View(0, "查看"),

		Edit(1, "编辑"),

		Delete(2, "删除"),

		Publish(3, "发布"),

		ShowHide(4, "显示/隐藏"),

		Move(5, "移动"),

		Sort(6, "排序"),

		AddContent(7, "新增内容"),

		EditContent(8, "编辑内容"),

		DeleteContent(9, "删除内容");

		/**
		 * 权限项在bitset中的位置序号，从0开始，不可随意变更，变更后会导致原权限信息错误
		 */
		private int bitIndex;

		private String label;

		CatalogPrivItem(int bitIndex, String label) {
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

		public String getPermissionKey(Long catalogId) {
			return ID + ":" + this.name() + ":" + catalogId;
		}
	}
}
