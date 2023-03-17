package com.ruoyi.contentcore.core.impl;

import org.springframework.stereotype.Component;

import com.ruoyi.contentcore.core.ICatalogType;

@Component(ICatalogType.BEAN_NAME_PREFIX + CatalogType_Common.ID)
public class CatalogType_Common implements ICatalogType {

	public final static String ID = "common";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "普通栏目";
	}
}
