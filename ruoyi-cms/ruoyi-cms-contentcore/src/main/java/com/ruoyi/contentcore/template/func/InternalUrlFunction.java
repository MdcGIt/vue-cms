package com.ruoyi.contentcore.template.func;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.staticize.func.AbstractFunc;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;

import freemarker.core.Environment;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;

/**
 * Freemarker模板自定义函数：处理内部链接
 */
@Component
@RequiredArgsConstructor
public class InternalUrlFunction extends AbstractFunc  {

	static final String FUNC_NAME = "internalUrl";

	@Override
	public String getFuncName() {
		return FUNC_NAME;
	}

	@Override
	public String getDesc() {
		return "将内部链接“iurl://”解析为正常http(s)访问地址，例如：${internalUrl(content.redirectUrl)}";
	}
	
	@Override
	public Object exec0(Object... args) throws TemplateModelException {
		if (args.length < 1) {
			return StringUtils.EMPTY;
		}
		TemplateContext context = FreeMarkerUtils.getTemplateContext(Environment.getCurrentEnvironment());
		SimpleScalar simpleScalar = (SimpleScalar) args[0];
		return InternalUrlUtils.getActualUrl(simpleScalar.getAsString(), context.getPublishPipeCode(), context.isPreview());
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("内部链接", FuncArgType.String, true, null));
	}
}
