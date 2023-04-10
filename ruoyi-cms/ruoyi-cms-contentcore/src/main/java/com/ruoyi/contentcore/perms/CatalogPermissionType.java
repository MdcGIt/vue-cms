package com.ruoyi.contentcore.perms;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.util.CmsPrivUtils;
import com.ruoyi.system.permission.IPermissionType;

/**
 * 栏目权限类型
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Component(IPermissionType.BEAN_PREFIX + CatalogPermissionType.ID)
public class CatalogPermissionType implements IPermissionType {

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
	public String convert(List<String> permissionKeys) {
		return CmsPrivUtils.convertCatalogPermissionKeys(permissionKeys);
	}

	/**
	 * {<siteId: [long]>,...}
	 */
	@Override
	public List<String> parse(String json) {
		return CmsPrivUtils.parseCatalogPermissionJson(json);
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

		public String getPermissionKey(String catalogId) {
			return ID + ":" + this.name() + ":" + catalogId;
		}
	}
}
