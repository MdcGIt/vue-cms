package com.ruoyi.contentcore.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.service.ISysDictTypeService;

/**
 * 启用/禁用
 */
@Component(FixedDictType.BEAN_PREFIX + ContentStatus.TYPE)
public class ContentStatus extends FixedDictType {

	public static final String TYPE = "CMSContentStatus";

	public static final String DRAFT = "0"; // 初稿

	public static final String TO_PUBLISHED = "20"; // 待发布

	public static final String PUBLISHED = "30"; // 已发布

	public static final String OFFLINE = "40"; // 已下线

	public static final String EDITING = "60"; // 重新编辑

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public ContentStatus() {
		super(TYPE, "DICT." + TYPE);
		super.addDictData("DICT." + TYPE + "." + DRAFT, DRAFT, 1);
		super.addDictData("DICT." + TYPE + "." + TO_PUBLISHED, TO_PUBLISHED, 2);
		super.addDictData("DICT." + TYPE + "." + PUBLISHED, PUBLISHED, 3);
		super.addDictData("DICT." + TYPE + "." + OFFLINE, OFFLINE, 4);
		super.addDictData("DICT." + TYPE + "." + EDITING, EDITING, 5);
	}

	public static boolean isPublished(String v) {
		return PUBLISHED.equals(v);
	}
	
	public static boolean isToPublishOrPublished(String v) {
		return PUBLISHED.equals(v) || TO_PUBLISHED.equals(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
