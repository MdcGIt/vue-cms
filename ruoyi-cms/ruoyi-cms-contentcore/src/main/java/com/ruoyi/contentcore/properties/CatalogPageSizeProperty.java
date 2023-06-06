package com.ruoyi.contentcore.properties;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 内容列表页的每页默认条数
 *
 * @author 未来
 * @mail 959009@qq.com
 */
@Component(IProperty.BEAN_NAME_PREFIX + CatalogPageSizeProperty.ID)
public class CatalogPageSizeProperty implements IProperty {

	public final static String ID = "CatalogPageSize";

	static UseType[] UseTypes = new UseType[] { UseType.Site, UseType.Catalog };
	
	@Override
	public UseType[] getUseTypes() {
		return UseTypes;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "内容列表页的每页默认条数";
	}
	
	@Override
	public boolean validate(String value) {
		return StringUtils.isEmpty(value) || NumberUtils.isCreatable(value.toString());
	}
	
	@Override
	public Integer defaultValue() {
		return -1;
	}
	
	@Override
	public Integer getPropValue(Map<String, String> configProps) {
		String string = MapUtils.getString(configProps, getId());
		if (NumberUtils.isDigits(string)) {
			return NumberUtils.toInt(string);
		}
		return defaultValue();
	}
	
	public static int getValue(Map<String, String> catalogProps, Map<String, String> siteProps) {
		int pageSize = ConfigPropertyUtils.getIntValue(ID, catalogProps);
		if (pageSize < 1) {
			pageSize = ConfigPropertyUtils.getIntValue(ID, siteProps);
		}
		return pageSize;
	}
}