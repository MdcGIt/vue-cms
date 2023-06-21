package com.ruoyi.customform.fixed.dict;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.domain.SysDictData;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.service.ISysDictTypeService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 元数据字段类型
 */
@Component(FixedDictType.BEAN_PREFIX + CustomFormStatus.TYPE)
public class CustomFormStatus extends FixedDictType {

	public static final String TYPE = "CustomFormStatus";

	public static final int DRAFT = 0; // 草稿

	public static final int PUBLISHED = 10; // 已发布

	public static final int OFFLINE = 20; // 已下线


	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public CustomFormStatus() {
		super(TYPE, "{DICT." + TYPE + "}");
		super.addDictData("{DICT." + TYPE + "." + DRAFT + "}", String.valueOf(DRAFT), 1);
		super.addDictData("{DICT." + TYPE + "." + PUBLISHED + "}", String.valueOf(PUBLISHED), 2);
		super.addDictData("{DICT." + TYPE + "." + OFFLINE + "}", String.valueOf(OFFLINE), 3);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
