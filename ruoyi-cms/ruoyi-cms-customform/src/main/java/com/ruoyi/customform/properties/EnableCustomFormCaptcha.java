package com.ruoyi.customform.properties;

import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IProperty;
import com.ruoyi.contentcore.util.ConfigPropertyUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 自定义表单提交是否需要验证码
 */
@Component(IProperty.BEAN_NAME_PREFIX + EnableCustomFormCaptcha.ID)
public class EnableCustomFormCaptcha implements IProperty {

	public final static String ID = "EnableCustomFormCaptcha";
	
	static UseType[] UseTypes = new UseType[] { UseType.Site };
	
	@Override
	public UseType[] getUseTypes() {
		return UseTypes;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "自定义表单提交开启验证码";
	}

	@Override
	public String defaultValue() {
		return YesOrNo.NO;
	}

	public static boolean getValue(Map<String, String> props) {
		String v = ConfigPropertyUtils.getStringValue(ID, props);
		if (StringUtils.isEmpty(v)) {
			v = YesOrNo.NO;
		}
		return YesOrNo.isYes(v);
	}
}
