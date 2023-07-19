package com.ruoyi.contentcore.template.func;

import com.ruoyi.common.staticize.func.AbstractFunc;
import com.ruoyi.common.utils.StringUtils;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Freemarker模板自定义函数：三元表达式
 */
@Component
@RequiredArgsConstructor
public class IfElseFunction extends AbstractFunc {

	static final String FUNC_NAME = "ifElse";

	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	@Override
	public String getFuncName() {
		return FUNC_NAME;
	}

	@Override
	public String getDesc() {
		return DESC;
	}

	@Override
	public Object exec0(Object... args) throws TemplateModelException {
		if (args.length == 3 && args[0] instanceof TemplateBooleanModel condition) {
			return condition.getAsBoolean() ? args[1] : args[2];
		}
		return StringUtils.EMPTY;
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(
				new FuncArg("条件值", FuncArgType.Boolean, true, null),
				new FuncArg("条件成立返回值", FuncArgType.String, true, null),
				new FuncArg("条件不成立返回值", FuncArgType.String, true, null));
	}
}
