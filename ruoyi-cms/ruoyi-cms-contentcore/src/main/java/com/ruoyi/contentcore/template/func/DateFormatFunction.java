package com.ruoyi.contentcore.template.func;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.ruoyi.common.staticize.func.AbstractFunc;
import com.ruoyi.common.utils.ConvertUtils;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;

import freemarker.ext.beans.BeanModel;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;

/**
 * Freemarker模板自定义函数：日期格式化
 */
@Component
@RequiredArgsConstructor
public class DateFormatFunction extends AbstractFunc {

	static final String FUNC_NAME = "dateFormat";
	
	private static final String DESC = "{FREEMARKER.FUNC.DESC." + FUNC_NAME + "}";

	private static final SimpleDateFormat DEFAULT_SIMPLE_DATE_FORMAT = new SimpleDateFormat(
			DateUtils.YYYY_MM_DD_HH_MM_SS);

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
		if (args.length < 1 || Objects.isNull(args[0])) {
			return StringUtils.EMPTY;
		}
		if (args[0] instanceof BeanModel model) {
			Object obj = model.getWrappedObject();

			if (obj instanceof TemporalAccessor t) {
				if (args.length > 1) {
					return DateTimeFormatter.ofPattern(args[1].toString()).format(t);
				} else {
					return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(t);
				}
			} else if (obj instanceof Date d) {
				if (args.length > 1) {
					String formatStr = ConvertUtils.toStr(args[1], DateUtils.YYYY_MM_DD_HH_MM_SS);
					return DateUtils.parseDateToStr(formatStr, d);
				} else {
					return DEFAULT_SIMPLE_DATE_FORMAT.format(d);
				}
			}
		}
		return args[0].toString();
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("日期时间", FuncArgType.DateTime, true, null),
				new FuncArg("格式化字符串", FuncArgType.String, false, "默认格式：yyyy-MM-dd HH:mm:ss"));
	}
}
