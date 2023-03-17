package com.ruoyi.system.fixed.dict;

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
@Component(FixedDictType.BEAN_PREFIX + UserStatus.TYPE)
public class UserStatus extends FixedDictType {

	public static final String TYPE = "SysUserStatus";

	public static final String ENABLE = "0"; // 正常
	
	public static final String DISABLE = "1"; // 禁用
	
	public static final String LOCK = "2"; // 锁定
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);
	
	public UserStatus() {
		super(TYPE, "DICT." + TYPE);
		super.addDictData("DICT." + TYPE + "." + ENABLE, ENABLE, 1);
		super.addDictData("DICT." + TYPE + "." + DISABLE, DISABLE, 2);
		super.addDictData("DICT." + TYPE + "." + LOCK, LOCK, 3);
	}
	
	public static boolean isEnable(String v) {
		return ENABLE.equals(v);
	}
	
	public static boolean isDisbale(String v) {
		return DISABLE.equals(v);
	}
	
	public static boolean isLocked(String v) {
		return LOCK.equals(v);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
