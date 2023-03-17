package com.ruoyi.advertisement;

import org.springframework.stereotype.Component;

@Component(IAdvertisementType.BEAN_NAME_PREFIX + ImageAdvertisementType.ID)
public class ImageAdvertisementType implements IAdvertisementType {
	
	public final static String ID = "image";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "ADVERTISEMENT.TYPE." + ID;
	}
}
