package com.ruoyi.exmodel.fixed.dict;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.service.ISysDictTypeService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 扩展模型扩展数据类型
 */
@Component(FixedDictType.BEAN_PREFIX + ExtendModelDataType.TYPE)
public class ExtendModelDataType extends FixedDictType {

	public static final String TYPE = "ExtendModelDataType";

	public static final String SITE = "site"; // 站点

	public static final String CATALOG = "catalog"; // 栏目

	public static final String CONTENT = "content"; // 内容


	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public ExtendModelDataType() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + SITE + "}", SITE, 1);
		super.addDictData("{DICT." + TYPE + "." + CATALOG + "}", CATALOG, 2);
		super.addDictData("{DICT." + TYPE + "." + CONTENT + "}", CONTENT, 3);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
