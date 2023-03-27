package com.ruoyi.vote.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.service.ISysDictTypeService;

/**
 * 调查投票主题类型
 */
@Component(FixedDictType.BEAN_PREFIX + VoteSubjectType.TYPE)
public class VoteSubjectType extends FixedDictType {

	public static final String TYPE = "VoteSubjectType";

	public static final String RADIO = "radio"; // 单选

	public static final String CHECKBOX = "checkbox"; // 多选

	public static final String INPUT = "input"; // 输入
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public VoteSubjectType() {
		super(TYPE, "DICT." + TYPE);
		super.addDictData("DICT." + TYPE + "." + RADIO, RADIO, 1);
		super.addDictData("DICT." + TYPE + "." + CHECKBOX, CHECKBOX, 2);
		super.addDictData("DICT." + TYPE + "." + INPUT, INPUT, 3);
	}
	
	public static boolean isInput(String type) {
		return INPUT.equals(type);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
