package com.ruoyi.system.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.service.ISysDictTypeService;

/**
 * 国际化语言标签
 */
@Component(FixedDictType.BEAN_PREFIX + I18nDictType.TYPE)
public class I18nDictType extends FixedDictType {

	public static final String TYPE = "I18nDictType";
	
	public static final String ZH_CN = "zh-CN";
	
	public static final String EN = "en";
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);
	
	public I18nDictType() {
		super(TYPE, "DICT." + TYPE);
		super.addDictData("DICT." + TYPE + "." + ZH_CN, ZH_CN, 1);
		super.addDictData("DICT." + TYPE + "." + EN, EN, 2);
	}
	
	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
