package com.ruoyi.contentcore.properties;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;

/**
 * 回收站内容保留天数
 */
@Component(IProperty.BEAN_NAME_PREFIX + RecycleKeepDaysProperty.ID)
public class RecycleKeepDaysProperty implements IProperty {

	public final static String ID = "RecycleKeepDays";
	
	static UseType[] UseTypes = new UseType[] { UseType.Site };
	
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
		return "回收站内容保留天数";
	}
	
	@Override
	public boolean validate(String value) {
		return StringUtils.isEmpty(value) || NumberUtils.isDigits(value.toString());
	}
	
	@Override
	public Integer defaultValue() {
		return 30;
	}
	
	@Override
	public Integer getPropValue(Map<String, String> configProps) {
		String string = MapUtils.getString(configProps, getId());
		if (NumberUtils.isDigits(string)) {
			return NumberUtils.toInt(string);
		}
		return defaultValue();
	}
	
	public static int getValue(Map<String, String> props) {
		return ConfigPropertyUtils.getIntValue(ID, props);
	}
}
