package com.ruoyi.cms.word.template.func;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.common.staticize.func.AbstractFunc;
import com.ruoyi.common.utils.ConvertUtils;
import com.ruoyi.common.utils.ObjectUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.word.WordConstants;
import com.ruoyi.word.service.IHotWordService;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;

/**
 * Freemarker模板自定义函数，添加热词链接
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Component
@RequiredArgsConstructor
public class ReplaceHotWord extends AbstractFunc  {

	private final IHotWordService hotWordService;
	
	static final String FUNC_NAME = "replaceHotWord";
	
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
		if (args.length < 2 || ObjectUtils.isAnyNull(args[0], args[1])) {
			return StringUtils.EMPTY;
		}
		String text = ((SimpleScalar) args[0]).getAsString();
		if (StringUtils.isEmpty(text)) {
			return StringUtils.EMPTY;
		}
		String groupCode = ((SimpleScalar) args[1]).getAsString();
		if (StringUtils.isEmpty(groupCode)) {
			return text;
		}
		String[] codes = StringUtils.split(groupCode, StringUtils.COMMA);
		String replacementTemplate = null;
		if (args.length == 3) {
			replacementTemplate = ConvertUtils.toStr(args[2]);
		}
		return this.hotWordService.replaceHotWords(text, codes, null, replacementTemplate);
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("待处理字符串", FuncArgType.String, true, null),
				new FuncArg("热词分组编码", FuncArgType.String, true, "多个热词分组用因为逗号分隔"),
				new FuncArg("自定义替换模板", FuncArgType.String, false, "默认：" + WordConstants.HOT_WORD_REPLACEMENT));
	}
}
