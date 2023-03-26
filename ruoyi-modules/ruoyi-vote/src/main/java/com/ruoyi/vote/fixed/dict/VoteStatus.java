package com.ruoyi.vote.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.service.ISysDictTypeService;

/**
 * 调查投票状态
 */
@Component(FixedDictType.BEAN_PREFIX + VoteStatus.TYPE)
public class VoteStatus extends FixedDictType {

	public static final String TYPE = "VoteStatus";

	public static final int NORMAL = 0; // 正常

	public static final int STOP = 1; // 停用

	public static final int CLOSE = 2; // 关闭，关闭的投票表示归档不可再变更
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public VoteStatus() {
		super(TYPE, "DICT." + TYPE);
		super.addDictData("DICT." + TYPE + "." + NORMAL, String.valueOf(NORMAL), 1);
		super.addDictData("DICT." + TYPE + "." + STOP, String.valueOf(STOP), 2);
		super.addDictData("DICT." + TYPE + "." + CLOSE, String.valueOf(CLOSE), 3);
	}
	
	public static boolean isClosed(int status) {
		return CLOSE == status;
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
