package com.ruoyi.cms.member.template.func;

import com.ruoyi.cms.member.utils.CmsMemberUtils;
import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.staticize.func.AbstractFunc;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ObjectUtils;
import com.ruoyi.common.utils.StringUtils;
import freemarker.core.Environment;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Freemarker模板自定义函数：清理html标签
 */
@Component
@RequiredArgsConstructor
public class AccountUrlFunction extends AbstractFunc  {

	private static final String FUNC_NAME = "accountUrl";

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
		if (args.length < 2 || ObjectUtils.isAnyNull(args)) {
			return StringUtils.EMPTY;
		}
		boolean includeBaseArgs = true;
		if (args.length > 2) {
			includeBaseArgs = ((TemplateBooleanModel) args[2]).getAsBoolean();
		}
		long memberId;
		if (args[0] instanceof  TemplateNumberModel tnm) {
			memberId = tnm.getAsNumber().longValue();
		} else {
			memberId = Long.parseLong(args[0].toString());
		}
		String type = args[1].toString();
		if (!IdUtils.validate(memberId)) {
			throw  new TemplateModelException("Can't use `accountUrl` function without Member.memberId.");
		}
		return CmsMemberUtils.getAccountUrl(memberId, type, Environment.getCurrentEnvironment(), includeBaseArgs);
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("会员ID", FuncArgType.Long, true, "数字或字符串"),
				new FuncArg("类型", FuncArgType.String, true, null),
				new FuncArg("是否带站点ID和发布通道参数", FuncArgType.String, false, "默认：true"));
	}
}
