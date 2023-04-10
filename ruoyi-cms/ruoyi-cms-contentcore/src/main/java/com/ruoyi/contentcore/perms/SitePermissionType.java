package com.ruoyi.contentcore.perms;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.util.CmsPrivUtils;
import com.ruoyi.system.permission.IPermissionType;

/**
 * 站点权限类型
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Component(IPermissionType.BEAN_PREFIX + SitePermissionType.ID)
public class SitePermissionType implements IPermissionType {

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
	public String convert(List<String> permissionKeys) {
		return CmsPrivUtils.convertSitePermissionKeys(permissionKeys);
	}

	/**
	 * {<siteId: [long]>,...}
	 */
	@Override
	public List<String> parse(String json) {
		return CmsPrivUtils.parseSitePermissionJson(json);
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

		public String getPermissionKey(String siteId) {
			return ID + ":" + this.name() + ":" + siteId;
		}
	}
}
