package com.ruoyi.contentcore.core.impl;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.ICatalogType;

@Component(ICatalogType.BEAN_NAME_PREFIX + CatalogType_Link.ID)
public class CatalogType_Link implements ICatalogType {

	public final static String ID = "link";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "链接栏目";
	}
}
