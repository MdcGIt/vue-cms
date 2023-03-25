package com.ruoyi.comment.fixed.dict;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.service.ISysDictTypeService;

/**
 * 评论审核状态
 */
@Component(FixedDictType.BEAN_PREFIX + CommentAuditStatus.TYPE)
public class CommentAuditStatus extends FixedDictType {

	public static final String TYPE = "CommentAuditStatus";

	public static final int TO_AUDIT = 0; // 待审核

	public static final int PASSED = 1; // 审核通过

	public static final int NOT_PASSED = 2; // 审核不通过
	
	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public CommentAuditStatus() {
		super(TYPE, "DICT." + TYPE);
		super.addDictData("DICT." + TYPE + "." + TO_AUDIT, String.valueOf(TO_AUDIT), 1);
		super.addDictData("DICT." + TYPE + "." + PASSED, String.valueOf(PASSED), 2);
		super.addDictData("DICT." + TYPE + "." + NOT_PASSED, String.valueOf(NOT_PASSED), 3);
	}
	
	public static boolean isPassed(int v) {
		return PASSED == v;
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
