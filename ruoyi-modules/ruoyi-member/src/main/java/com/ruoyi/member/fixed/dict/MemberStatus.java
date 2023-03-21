package com.ruoyi.member.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.service.ISysDictTypeService;

/**
 * 用户状态
 */
@Component(FixedDictType.BEAN_PREFIX + MemberStatus.TYPE)
public class MemberStatus extends FixedDictType {

	public static final String TYPE = "CCMemberStatus";

	public static final String ENABLE = "0"; // 正常
	
	public static final String DISABLE = "1"; // 禁用
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);
	
	public MemberStatus() {
		super(TYPE, "DICT." + TYPE);
		super.addDictData("DICT." + TYPE + "." + ENABLE, ENABLE, 1);
		super.addDictData("DICT." + TYPE + "." + DISABLE, DISABLE, 2);
	}
	
	public static boolean isEnable(String v) {
		return ENABLE.equals(v);
	}
	
	public static boolean isDisbale(String v) {
		return DISABLE.equals(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
