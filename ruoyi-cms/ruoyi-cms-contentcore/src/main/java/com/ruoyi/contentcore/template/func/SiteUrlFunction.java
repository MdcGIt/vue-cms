package com.ruoyi.contentcore.template.func;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ruoyi.common.staticize.FreeMarkerUtils;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.staticize.func.AbstractFunc;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.util.SiteUtils;

import freemarker.core.Environment;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModelException;
import lombok.RequiredArgsConstructor;

/**
 * Freemarker模板自定义函数：处理发布通道站点URL
 */
@Component
@RequiredArgsConstructor
public class SiteUrlFunction extends AbstractFunc {

	private static final String FUNC_NAME = "siteUrl";

	private final ISiteService siteService;

	@Override
	public String getFuncName() {
		return FUNC_NAME;
	}

	@Override
	public String getDesc() {
		return "获取站点指定发布通道访问链接，例如：${siteUrl(Site.siteId, 'h5')}";
	}

	@Override
	public Object exec0(Object... args) throws TemplateModelException {
		if (args.length != 2) {
			return StringUtils.EMPTY;
		}
		long siteId = ((SimpleNumber) args[0]).getAsNumber().longValue();
		String publishPipeCode = ((SimpleScalar) args[1]).getAsString();
		TemplateContext context = FreeMarkerUtils.getTemplateContext(Environment.getCurrentEnvironment());
		return SiteUtils.getSiteLink(siteService.getSite(siteId), publishPipeCode, context.isPreview());
	}

	@Override
	public List<FuncArg> getFuncArgs() {
		return List.of(new FuncArg("站点ID", FuncArgType.Long, true, null),
				new FuncArg("发布通道编码", FuncArgType.String, true, null));
	}
}
