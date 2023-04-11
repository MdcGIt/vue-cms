package com.ruoyi.system.fixed.dict;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import com.ruoyi.common.utils.ChineseSpelling;
import com.ruoyi.common.utils.SpringUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.fixed.FixedDictType;
import com.ruoyi.system.security.ISecurityUser;
import com.ruoyi.system.service.ISysDictTypeService;

/**
 * 密码敏感信息配置
 */
@Component(FixedDictType.BEAN_PREFIX + PasswordSensitive.TYPE)
public class PasswordSensitive extends FixedDictType {

	public static final String TYPE = "SecurityPasswordSensitive";

	public static final String ACCOUNT = "ACCOUNT"; // 用户名

	public static final String PHONE_NUMBER = "PHONE_NUMBER"; // 手机号

	public static final String EMAIL = "EMAIL"; // email
	
	public static final String NICK_NAME = "NICK_NAME"; // 昵称
	
	public static final String REAL_NAME = "REAL_NAME"; // 真实姓名
	
	public static final String BIRTHDAY = "BIRTHDAY"; // 出生日期

	private static final ISysDictTypeService dictTypeService = SpringUtils.getBean(ISysDictTypeService.class);

	public PasswordSensitive() {
		super(TYPE, "{DICT." + TYPE + "}", true);
		super.addDictData("{DICT." + TYPE + "." + ACCOUNT + "}", ACCOUNT, 1);
		super.addDictData("{DICT." + TYPE + "." + PHONE_NUMBER + "}", PHONE_NUMBER, 2);
		super.addDictData("{DICT." + TYPE + "." + EMAIL + "}", EMAIL, 3);
		super.addDictData("{DICT." + TYPE + "." + NICK_NAME + "}", NICK_NAME, 4);
		super.addDictData("{DICT." + TYPE + "." + REAL_NAME + "}", REAL_NAME, 5);
		super.addDictData("{DICT." + TYPE + "." + BIRTHDAY + "}", BIRTHDAY, 6);
	}

	public static boolean check(String[] values, String password, ISecurityUser user) {
		if (StringUtils.isEmpty(password) || StringUtils.isEmpty(values) || Objects.isNull(user)) {
			return true;
		}
		for (String v : values) {
			if (ACCOUNT.equals(v) && password.contains(user.getUserName())) {
				return false;
			} else if (PHONE_NUMBER.equals(v) && StringUtils.isNotEmpty(user.getPhonenumber()) && password.contains(user.getPhonenumber())) {
				return false;
			} else if (EMAIL.equals(v) && StringUtils.isNotEmpty(user.getEmail()) && password.contains(user.getEmail().substring(0, user.getEmail().lastIndexOf("@")))) {
				return false;
			} else if (NICK_NAME.equals(v) && StringUtils.isNotEmpty(user.getNickName()) && password.contains(ChineseSpelling.getSpelling(user.getNickName()))) {
				return false;
			} else if (REAL_NAME.equals(v) && StringUtils.isNotEmpty(user.getRealName()) && password.contains(ChineseSpelling.getSpelling(user.getRealName()))) {
				return false;
			} else if (BIRTHDAY.equals(v) && Objects.nonNull(user.getBirthday()) && password.contains(user.getBirthday().format(DateTimeFormatter.ofPattern("yyyyMMdd")))) {
				return false;
			}
		}
		return true;
	}

	public static <T> void decode(List<T> list, Function<T, String> getter, BiConsumer<T, String> setter) {
		dictTypeService.decode(TYPE, list, getter, setter);
	}
}
