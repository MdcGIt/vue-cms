package com.ruoyi.xmodel.fixed.dict;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.domain.SysDictData;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.service.ISysDictTypeService;

/**
 * 元数据字段类型
 */
@Component(FixedDictType.BEAN_PREFIX + MetaFieldType.TYPE)
public class MetaFieldType extends FixedDictType {

	public static final String TYPE = "MetaFieldType";

	public static final String SHORT_TEXT = "short_text";

	public static final String MEDIUM_TEXT = "medium_text";

	public static final String LARGE_TEXT = "large_text";

	public static final String CLOB_TEXT = "clob_text";

	public static final String LONG = "long";

	public static final String DOUBLE = "double";

	public static final String DATETIME = "datetime";

	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	/**
	 * 字典项备注=可用字段数量
	 */
	public MetaFieldType() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + SHORT_TEXT + "}", SHORT_TEXT, 1, "25");
		super.addDictData("{DICT." + TYPE + "." + MEDIUM_TEXT + "}", MEDIUM_TEXT, 2, "25");
		super.addDictData("{DICT." + TYPE + "." + LARGE_TEXT + "}", LARGE_TEXT, 3, "4");
		super.addDictData("{DICT." + TYPE + "." + CLOB_TEXT + "}", CLOB_TEXT, 4, "1");
		super.addDictData("{DICT." + TYPE + "." + LONG + "}", LONG, 5, "10");
		super.addDictData("{DICT." + TYPE + "." + DOUBLE + "}", DOUBLE, 6, "10");
		super.addDictData("{DICT." + TYPE + "." + DATETIME + "}", DATETIME, 7, "10");
	}
    
	/**
	 * 获取字段可用数量
	 * 
	 * @param fieldType
	 * @return
	 */
    public static int getFieldTypeLimit(String fieldType) {
    	Optional<SysDictData> findFirst = dictTypeService.selectDictDatasByType(TYPE).stream().filter(d -> d.getDictValue().equals(fieldType)).findFirst();
    	if(findFirst.isPresent()) {
        	return NumberUtils.toInt(findFirst.get().getRemark());
    	}
    	return 0;
    }

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
