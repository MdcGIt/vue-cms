package com.ruoyi.system.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.service.ISysDictTypeService;

/**
 * 密码重试处理策略
 */
@Component(FixedDictType.BEAN_PREFIX + PasswordRetryStrategy.TYPE)
public class PasswordRetryStrategy extends FixedDictType {

	public static final String TYPE = "SecurityPasswordRetryStrategy";

	public static final String NONE = "NONE"; // 不处理

	public static final String DISABLE = "DISABLE"; // 封禁账号

	public static final String LOCK = "LOCK"; // 锁定用户，拒绝登录一段时间
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public PasswordRetryStrategy() {
		super(TYPE, "DICT." + TYPE, true);
		super.addDictData("DICT." + TYPE + "." + NONE, NONE, 1);
		super.addDictData("DICT." + TYPE + "." + DISABLE, DISABLE, 2);
		super.addDictData("DICT." + TYPE + "." + LOCK, LOCK, 3);
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
