package com.ruoyi.contentcore.perms;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.permission.IPermissionType;

@Component(IPermissionType.BEAN_PREFIX + SitePermissionType.ID)
public class SitePermissionType implements IPermissionType {

	public static final String ID = "site";

	public enum SiteSubPriv {

		Edit("编辑"),

		Delete("删除"),

		Publish("发布"),

		GrantPriv("授权");

		private String label;

		SiteSubPriv(String label) {
			this.label = label;
		}

		public String label() {
			return this.label;
		}

		public String getPermissionKey(String siteId) {
			return this.name() + ":" + siteId;
		}
	}

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
		Map<String, BitSet> map = new HashMap<>(); // <SiteId, List<SiteSubPriv>>
		permissionKeys.forEach(key -> {
			String[] split = StringUtils.split(key, ":");
			String siteId = split[1];
			BitSet bitSet = map.get(siteId);
			if (bitSet == null) {
				bitSet = new BitSet(SiteSubPriv.values().length);
				map.put(siteId, bitSet);
			}
			bitSet.set(SiteSubPriv.valueOf(split[0]).ordinal());
		});
		Map<String, long[]> permsMap = new HashMap<>();
		map.entrySet().forEach(e -> {
			permsMap.put(e.getKey(), e.getValue().toLongArray());
		});
		return JacksonUtils.to(permsMap);
	}

	/**
	 * {<siteId: [long]>,...}
	 */
	@Override
	public List<String> parsePermissionKeys(String json) {
		List<String> permissions = new ArrayList<>();
		Map<String, Long[]> sitePrivs = JacksonUtils.fromMap(json, Long[].class);
		if (sitePrivs != null) {
			sitePrivs.entrySet().forEach(e -> {
				BitSet bitSet = BitSet.valueOf(Stream.of(e.getValue()).mapToLong(Long::longValue).toArray());
				SiteSubPriv[] values = SiteSubPriv.values();
				for (int i = 0; i < values.length; i++) {
					if (bitSet.get(i)) {
						permissions.add(values[i].getPermissionKey(e.getKey()));
					}
				}
			});
		}
		return permissions;
	}
}
