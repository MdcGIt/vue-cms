package com.ruoyi.contentcore.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.staticize.StaticizeService;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.contentcore.core.IInternalDataType;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.exception.ContentCoreErrorCode;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.service.ITemplateService;
import com.ruoyi.contentcore.template.ITemplateType;
import com.ruoyi.contentcore.template.impl.SiteTemplateType;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.contentcore.util.TemplateUtils;

import freemarker.template.TemplateException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 内容核心管理
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class CoreController extends BaseRestController {

	private final ISiteService siteService;

	private final ITemplateService templateService;

	private final StaticizeService staticizeService;

	/**
	 * 预览内容核心数据页面
	 * 
	 * @param dataType    内容核心数据类型ID
	 * @param dataId      内容核心数据ID
	 * @param publishPipe 发布通道编码
	 * @param pageIndex   页码
	 * @throws IOException
	 * @throws TemplateException
	 */
	@GetMapping("/cms/preview/{dataType}/{dataId}")
	public void preview(@PathVariable("dataType") String dataType, @PathVariable("dataId") Long dataId,
			@RequestParam(value = "pp") String publishPipe,
			@RequestParam(value = "pi", required = false, defaultValue = "1") Integer pageIndex,
			@RequestParam(value = "list", required = false, defaultValue = "N") String listFlag)
			throws IOException, TemplateException {
		HttpServletResponse response = ServletUtils.getResponse();
		response.setCharacterEncoding(Charset.defaultCharset().displayName());
		response.setHeader("contentType", "text/html; charset=" + Charset.defaultCharset().displayName());
		IInternalDataType internalDataType = ContentCoreUtils.getInternalDataType(dataType);
		Assert.notNull(internalDataType, () -> ContentCoreErrorCode.UNSUPPORT_INTERNAL_DATA_TYPE.exception(dataType));

		IInternalDataType.RequestData data = new IInternalDataType.RequestData(dataId, pageIndex, publishPipe,
				true, ServletUtils.getParamMap(ServletUtils.getRequest()));
		String pageData = internalDataType.getPageData(data);
		response.getWriter().write(pageData);
	}

	/**
	 * 浏览内部数据页面
	 * 
	 * @param dataType    内容核心数据类型ID
	 * @param dataId      内容核心数据ID
	 * @param publishPipe 发布通道编码
	 * @param pageIndex   页码
	 * @throws IOException
	 * @throws TemplateException
	 */
	@GetMapping("/cms/view/{dataType}/{dataId}")
	public void browse(@PathVariable("dataType") String dataType, @PathVariable("dataId") Long dataId,
			@RequestParam(value = "pp") String publishPipe,
			@RequestParam(value = "pi", required = false, defaultValue = "1") Integer pageIndex)
			throws IOException, TemplateException {
		HttpServletResponse response = ServletUtils.getResponse();

		response.setCharacterEncoding(Charset.defaultCharset().displayName());
		response.setHeader("contentType", "text/html; charset=" + Charset.defaultCharset().displayName());
		IInternalDataType internalDataType = ContentCoreUtils.getInternalDataType(dataType);
		Assert.notNull(internalDataType, () -> ContentCoreErrorCode.UNSUPPORT_INTERNAL_DATA_TYPE.exception(dataType));

		IInternalDataType.RequestData data = new IInternalDataType.RequestData(dataId, pageIndex, publishPipe,
				false, ServletUtils.getParamMap(ServletUtils.getRequest()));
		String pageData = internalDataType.getPageData(data);
		response.getWriter().write(pageData);
	}

	@GetMapping("/cms/ssi/virtual/")
	public void getSSIVirtualContent(@RequestParam("sid") Long siteId, @RequestParam("pp") String publishPipeCode,
			@RequestParam("t") String template,
			@RequestParam(value = "pi", required = false, defaultValue = "1") Integer pageIndex,
			@RequestParam Map<String, Object> params) {
		try {
			long s = System.currentTimeMillis();
			CmsSite site = this.siteService.getSite(siteId);
			// 模板ID = 通道:站点目录:模板文件名
			String templateId = SiteUtils.getTemplateName(site, publishPipeCode, template);
			// 缓存
			String templateStaticContentCache = this.templateService.getTemplateStaticContentCache(templateId);
			if (Objects.nonNull(templateStaticContentCache)) {
				ServletUtils.getResponse().getWriter().write(templateStaticContentCache);
				return;
			}
			TemplateContext templateContext = new TemplateContext(templateId, false, publishPipeCode);
			templateContext.setPageIndex(pageIndex);
			// init template datamode
			TemplateUtils.initGlobalVariables(site, templateContext);
			// init templateType data to datamode
			ITemplateType templateType = this.templateService.getTemplateType(SiteTemplateType.TypeId);
			templateType.initTemplateData(siteId, templateContext);
			templateContext.getVariables().put("Request", params);
			templateContext.getVariables().put("ClientType", ServletUtils.getDeviceType());
			// staticize
			this.staticizeService.process(templateContext, ServletUtils.getResponse().getWriter());
			log.debug("[{}]动态区块模板解析：{}，耗时：{}", publishPipeCode, template, System.currentTimeMillis() - s);
		} catch (TemplateException | IOException e) {
			e.printStackTrace();
		}
	}
}
