package com.ruoyi.cms.search.controller.front;

import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.staticize.StaticizeService;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.service.ITemplateService;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.contentcore.util.TemplateUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * 搜索动态页面
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/_search")
public class CmsSearchController extends BaseRestController {

    private final ISiteService siteService;

    private final StaticizeService staticizeService;

    private final ITemplateService templateService;

    @GetMapping
    public void accountCentre(@RequestParam(value ="q", required = false, defaultValue = "") @Length(max = 50) String query,
                              @RequestParam("sid") Long siteId,
                              @RequestParam("pp") String publishPipeCode,
                              @RequestParam(value = "ot", required = false ,defaultValue = "false") Boolean onlyTitle,
                              @RequestParam(value = "ct", required = false) String contentType,
                              @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                              @RequestParam(required = false, defaultValue = "false") Boolean preview,
                              HttpServletResponse response)
            throws IOException {
        response.setCharacterEncoding(Charset.defaultCharset().displayName());
        response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

        CmsSite site = this.siteService.getSite(siteId);
        if (Objects.isNull(site)) {
            this.catchException(response, new RuntimeException("Site not found: " + siteId));
            return;
        }
        // 查找动态模板
        final String detailTemplate = "search.template.html"; // TODO 站点动态模板配置
        File templateFile = this.templateService.findTemplateFile(site, detailTemplate, publishPipeCode);
        if (Objects.isNull(templateFile) || !templateFile.exists()) {
            this.catchException(response, new RuntimeException("Template not found: " + detailTemplate));
            return;
        }
        long s = System.currentTimeMillis();
        // 生成静态页面
        try {
            // 模板ID = 通道:站点目录:模板文件名
            String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, detailTemplate);
            TemplateContext templateContext = new TemplateContext(templateKey, preview, publishPipeCode);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType data to datamode
            templateContext.getVariables().put("Request", ServletUtils.getParameters());

            String link = "_search?q=" + query;
            if (preview) {
                link += "&sid=" + siteId + "&pp=" + publishPipeCode + "&preview=true";
            }
            if (onlyTitle) {
                link += "&ot=true";
            }
            if (StringUtils.isNotEmpty(contentType)) {
                link += "&ct=" + contentType;
            }
            templateContext.setFirstFileName(link);
            templateContext.setOtherFileName(link + "&page=" + TemplateContext.PlaceHolder_PageNo);
            // staticize
            this.staticizeService.process(templateContext, response.getWriter());
            log.debug("搜索页面模板解析，耗时：{} ms", System.currentTimeMillis() - s);
        } catch (Exception e) {
            this.catchException(response, e);
        }
    }

    private void catchException(HttpServletResponse response, Exception e) throws IOException {
        if (log.isDebugEnabled()) {
            e.printStackTrace(response.getWriter());
        } else {
            response.sendRedirect("/500.html"); // TODO 通过发布通道属性配置错误页面
        }
    }
}
