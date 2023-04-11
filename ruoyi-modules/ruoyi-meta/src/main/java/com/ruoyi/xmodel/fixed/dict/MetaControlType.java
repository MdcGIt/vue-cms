package com.ruoyi.xmodel.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.service.ISysDictTypeService;

/**
 * 元数据控件类型
 */
@Component(FixedDictType.BEAN_PREFIX + MetaControlType.TYPE)
public class MetaControlType extends FixedDictType {

	public static final String TYPE = "MetaControlType";

	public static final String INPUT = "input";

	public static final String TEXT_AREA = "textarea";

	public static final String RADIO = "radio";

	public static final String CHECK_BOX = "checkbox";

	public static final String SELECT = "select";

	public static final String DATE = "date";

	public static final String DATETIME = "datetime";

	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public MetaControlType() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + INPUT + "}", INPUT, 1);
		super.addDictData("{DICT." + TYPE + "." + TEXT_AREA + "}", TEXT_AREA, 2);
		super.addDictData("{DICT." + TYPE + "." + RADIO + "}", RADIO, 3);
		super.addDictData("{DICT." + TYPE + "." + CHECK_BOX + "}", CHECK_BOX, 4);
		super.addDictData("{DICT." + TYPE + "." + SELECT + "}", SELECT, 5);
		super.addDictData("{DICT." + TYPE + "." + DATE + "}", DATE, 6);
		super.addDictData("{DICT." + TYPE + "." + DATETIME + "}", DATETIME, 7);
	}
	
	public static boolean isCheckbox(String v) {
		return CHECK_BOX.equals(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
