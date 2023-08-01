package com.ruoyi.cms.member.controller.front;

import com.ruoyi.article.domain.CmsArticleDetail;
import com.ruoyi.article.service.IArticleService;
import com.ruoyi.cms.member.domain.vo.ContributeArticleVO;
import com.ruoyi.cms.member.publishpipe.*;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.staticize.StaticizeService;
import com.ruoyi.common.staticize.core.TemplateContext;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.core.IContent;
import com.ruoyi.contentcore.core.IContentType;
import com.ruoyi.contentcore.domain.CmsContent;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.IContentService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.contentcore.service.ITemplateService;
import com.ruoyi.contentcore.util.ContentCoreUtils;
import com.ruoyi.contentcore.util.InternalUrlUtils;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.contentcore.util.TemplateUtils;
import com.ruoyi.member.domain.Member;
import com.ruoyi.member.domain.vo.MemberCache;
import com.ruoyi.member.security.MemberUserType;
import com.ruoyi.member.security.StpMemberUtil;
import com.ruoyi.member.service.IMemberService;
import com.ruoyi.member.service.IMemberStatDataService;
import com.ruoyi.member.util.MemberUtils;
import com.ruoyi.system.validator.LongId;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * 会员个人中心
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class MemberAccountController extends BaseRestController {

    private final ISiteService siteService;

    private final IContentService contentService;

    private final StaticizeService staticizeService;

    private final ITemplateService templateService;

    private final IMemberService memberService;

    private final IMemberStatDataService memberStatDataService;

    @GetMapping("/{memberId}")
    public void accountCentre(@PathVariable @LongId Long memberId,
                              @RequestParam(value = "type", required = false, defaultValue = "") String type,
                              @RequestParam("sid") Long siteId,
                              @RequestParam("pp") String publishPipeCode,
                              @RequestParam(value = "page", required = false, defaultValue = "1") @Min(1) Integer page,
                              @RequestParam(required = false, defaultValue = "false") Boolean preview,
                              HttpServletResponse response)
            throws IOException {
        response.setCharacterEncoding(Charset.defaultCharset().displayName());
        response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

        CmsSite site = this.siteService.getSite(siteId);
        if (Objects.isNull(site)) {
            this.catchException("/", response, new RuntimeException("Site not found: " + siteId));
            return;
        }
        MemberCache member = this.memberStatDataService.getMemberCache(memberId);
        if (Objects.isNull(member)) {
            this.catchException(SiteUtils.getSiteLink(site, publishPipeCode, preview), response, new RuntimeException("Member not found: " + memberId));
            return;
        }
        // 查找动态模板
        final String detailTemplate = PublishPipeProp_AccountCentreTemplate.getValue(publishPipeCode, site.getPublishPipeProps());
        File templateFile = this.templateService.findTemplateFile(site, detailTemplate, publishPipeCode);
        if (Objects.isNull(templateFile) || !templateFile.exists()) {
            this.catchException(SiteUtils.getSiteLink(site, publishPipeCode, preview), response, new RuntimeException("Template not found: " + detailTemplate));
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
            templateContext.getVariables().put("Member", member);
            templateContext.getVariables().put("MemberResourcePrefix", MemberUtils.getMemberResourcePrefix(preview));
            templateContext.getVariables().put("Request", ServletUtils.getParameters());
            templateContext.setPageIndex(page);

            String link = "account/" + memberId + "?type=" + type;
            if (preview) {
                link += "&sid=" + siteId + "&pp=" + publishPipeCode + "&preview=true";
            }
            templateContext.setFirstFileName(link);
            templateContext.setOtherFileName(link + "&page=" + TemplateContext.PlaceHolder_PageNo);
            // staticize
            this.staticizeService.process(templateContext, response.getWriter());
            log.debug("会员主页模板解析：{}，耗时：{} ms", member.getMemberId(), System.currentTimeMillis() - s);
        } catch (Exception e) {
            this.catchException(SiteUtils.getSiteLink(site, publishPipeCode, preview), response, e);
        }
    }

    @GetMapping("/login")
    public void memberLogin(@RequestParam("sid") Long siteId,
                            @RequestParam("pp") String publishPipeCode,
                            @RequestParam(required = false, defaultValue = "false") Boolean preview,
                            HttpServletResponse response)
            throws IOException {
        response.setCharacterEncoding(Charset.defaultCharset().displayName());
        response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

        CmsSite site = this.siteService.getSite(siteId);
        if (Objects.isNull(site)) {
            this.catchException("/", response, new RuntimeException("Site not found: " + siteId));
            return;
        }
        // 查找动态模板
        final String template = PublishPipeProp_MemberLoginTemplate.getValue(publishPipeCode, site.getPublishPipeProps());
        simpleSitePage(template, site, publishPipeCode, preview, response);
    }

    @GetMapping("/register")
    public void memberRegister(@RequestParam("sid") Long siteId,
                            @RequestParam("pp") String publishPipeCode,
                            @RequestParam(required = false, defaultValue = "false") Boolean preview,
                            HttpServletResponse response)
            throws IOException {
        response.setCharacterEncoding(Charset.defaultCharset().displayName());
        response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

        CmsSite site = this.siteService.getSite(siteId);
        if (Objects.isNull(site)) {
            this.catchException("/", response, new RuntimeException("Site not found: " + siteId));
            return;
        }
        // 查找动态模板
        final String template = PublishPipeProp_MemberRegisterTemplate.getValue(publishPipeCode, site.getPublishPipeProps());
        simpleSitePage(template, site, publishPipeCode, preview, response);
    }

    @GetMapping("/forget_password")
    public void memberResetPassword(@RequestParam("sid") Long siteId,
                                    @RequestParam("pp") String publishPipeCode,
                                    @RequestParam(required = false, defaultValue = "false") Boolean preview,
                                    HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(Charset.defaultCharset().displayName());
        response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

        CmsSite site = this.siteService.getSite(siteId);
        if (Objects.isNull(site)) {
            this.catchException("/", response, new RuntimeException("Site not found: " + siteId));
            return;
        }
        // 查找动态模板
        final String template = PublishPipeProp_MemberForgetPasswordTemplate.getValue(publishPipeCode, site.getPublishPipeProps());
        simpleSitePage(template, site, publishPipeCode, preview, response);
    }

    private void simpleSitePage(String template, CmsSite site, String publishPipeCode, Boolean preview, HttpServletResponse response)
            throws IOException {
        File templateFile = this.templateService.findTemplateFile(site, template, publishPipeCode);
        if (Objects.isNull(templateFile) || !templateFile.exists()) {
            this.catchException(SiteUtils.getSiteLink(site, publishPipeCode, preview), response, new RuntimeException("Template not found: " + template));
            return;
        }
        long s = System.currentTimeMillis();
        // 生成静态页面
        try {
            // 模板ID = 通道:站点目录:模板文件名
            String templateKey = SiteUtils.getTemplateKey(site, publishPipeCode, template);
            TemplateContext templateContext = new TemplateContext(templateKey, preview, publishPipeCode);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType data to datamode
            templateContext.getVariables().put("Request", ServletUtils.getParameters());
            // staticize
            this.staticizeService.process(templateContext, response.getWriter());
            log.debug("会员登录页模板解析，耗时：{} ms", System.currentTimeMillis() - s);
        } catch (Exception e) {
            this.catchException(SiteUtils.getSiteLink(site, publishPipeCode, preview), response, e);
        }
    }

    @Priv(type = MemberUserType.TYPE)
    @GetMapping("/setting")
    public void accountSetting(@RequestParam Long sid,
                               @RequestParam String pp,
                               @RequestParam(required = false, defaultValue = "false") Boolean preview,
                               HttpServletResponse response)
            throws IOException {
        response.setCharacterEncoding(Charset.defaultCharset().displayName());
        response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

        CmsSite site = this.siteService.getSite(sid);
        if (Objects.isNull(site)) {
            this.catchException("/", response, new RuntimeException("Site not found: " + sid));
            return;
        }
        long memberId = StpMemberUtil.getLoginIdAsLong();
        Member member = this.memberService.getById(memberId);
        if (Objects.isNull(member)) {
            this.catchException(SiteUtils.getSiteLink(site, pp, preview), response, new RuntimeException("Member not found: " + memberId));
            return;
        }
        // 查找动态模板
        final String detailTemplate = PublishPipeProp_MemberSettingTemplate.getValue(pp, site.getPublishPipeProps());
        File templateFile = this.templateService.findTemplateFile(site, detailTemplate, pp);
        if (Objects.isNull(templateFile) || !templateFile.exists()) {
            this.catchException(SiteUtils.getSiteLink(site, pp, preview), response, new RuntimeException("Template not found: " + detailTemplate));
            return;
        }
        long s = System.currentTimeMillis();
        // 生成静态页面
        try {
            // 模板ID = 通道:站点目录:模板文件名
            String templateKey = SiteUtils.getTemplateKey(site, pp, detailTemplate);
            TemplateContext templateContext = new TemplateContext(templateKey, preview, pp);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType data to datamode
            templateContext.getVariables().put("Member", member);
            templateContext.getVariables().put("MemberResourcePrefix", MemberUtils.getMemberResourcePrefix(preview));
            templateContext.getVariables().put("Request", ServletUtils.getParameters());
            // staticize
            this.staticizeService.process(templateContext, response.getWriter());
            log.debug("会员信息设置页面模板解析：{}，耗时：{} ms", member.getMemberId(), System.currentTimeMillis() - s);
        } catch (Exception e) {
            this.catchException(SiteUtils.getSiteLink(site, pp, preview), response, e);
        }
    }

    @Priv(type = MemberUserType.TYPE)
    @GetMapping("/password")
    public void accountPassword(@RequestParam Long sid,
                                @RequestParam String pp,
                                @RequestParam(required = false, defaultValue = "false") Boolean preview,
                                HttpServletResponse response)
            throws IOException {
        response.setCharacterEncoding(Charset.defaultCharset().displayName());
        response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

        CmsSite site = this.siteService.getSite(sid);
        if (Objects.isNull(site)) {
            this.catchException("/", response, new RuntimeException("Site not found: " + sid));
            return;
        }
        long memberId = StpMemberUtil.getLoginIdAsLong();
        Member member = this.memberService.getById(memberId);
        if (Objects.isNull(member)) {
            this.catchException(SiteUtils.getSiteLink(site, pp, preview), response, new RuntimeException("Member not found: " + memberId));
            return;
        }
        // 查找动态模板
        final String detailTemplate = PublishPipeProp_MemberPasswordTemplate.getValue(pp, site.getPublishPipeProps());
        File templateFile = this.templateService.findTemplateFile(site, detailTemplate, pp);
        if (Objects.isNull(templateFile) || !templateFile.exists()) {
            this.catchException(SiteUtils.getSiteLink(site, pp, preview), response, new RuntimeException("Template not found: " + detailTemplate));
            return;
        }
        long s = System.currentTimeMillis();
        // 生成静态页面
        try {
            // 模板ID = 通道:站点目录:模板文件名
            String templateKey = SiteUtils.getTemplateKey(site, pp, detailTemplate);
            TemplateContext templateContext = new TemplateContext(templateKey, preview, pp);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType data to datamode
            templateContext.getVariables().put("Member", member);
            templateContext.getVariables().put("MemberResourcePrefix", MemberUtils.getMemberResourcePrefix(preview));
            templateContext.getVariables().put("Request", ServletUtils.getParameters());
            // staticize
            this.staticizeService.process(templateContext, response.getWriter());
            log.debug("会员信息设置页面模板解析：{}，耗时：{} ms", member.getMemberId(), System.currentTimeMillis() - s);
        } catch (Exception e) {
            this.catchException(SiteUtils.getSiteLink(site, pp, preview), response, e);
        }
    }

    @Priv(type = MemberUserType.TYPE)
    @GetMapping("/change_email")
    public void accountChangeEmail(@RequestParam Long sid,
                                   @RequestParam String pp,
                                   @RequestParam(required = false, defaultValue = "false") Boolean preview,
                                   HttpServletResponse response)
            throws IOException {
        response.setCharacterEncoding(Charset.defaultCharset().displayName());
        response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

        CmsSite site = this.siteService.getSite(sid);
        if (Objects.isNull(site)) {
            this.catchException("/", response, new RuntimeException("Site not found: " + sid));
            return;
        }
        long memberId = StpMemberUtil.getLoginIdAsLong();
        Member member = this.memberService.getById(memberId);
        if (Objects.isNull(member)) {
            this.catchException(SiteUtils.getSiteLink(site, pp, preview), response, new RuntimeException("Member not found: " + memberId));
            return;
        }
        // 查找动态模板
        final String detailTemplate = PublishPipeProp_MemberBindEmailTemplate.getValue(pp, site.getPublishPipeProps());
        File templateFile = this.templateService.findTemplateFile(site, detailTemplate, pp);
        if (Objects.isNull(templateFile) || !templateFile.exists()) {
            this.catchException(SiteUtils.getSiteLink(site, pp, preview), response, new RuntimeException("Template not found: " + detailTemplate));
            return;
        }
        long s = System.currentTimeMillis();
        // 生成静态页面
        try {
            // 模板ID = 通道:站点目录:模板文件名
            String templateKey = SiteUtils.getTemplateKey(site, pp, detailTemplate);
            TemplateContext templateContext = new TemplateContext(templateKey, preview, pp);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType data to datamode
            templateContext.getVariables().put("Member", member);
            templateContext.getVariables().put("MemberResourcePrefix", MemberUtils.getMemberResourcePrefix(preview));
            templateContext.getVariables().put("Request", ServletUtils.getParameters());
            // staticize
            this.staticizeService.process(templateContext, response.getWriter());
            log.debug("会员信息设置页面模板解析：{}，耗时：{} ms", member.getMemberId(), System.currentTimeMillis() - s);
        } catch (Exception e) {
            this.catchException(SiteUtils.getSiteLink(site, pp, preview), response, e);
        }
    }

    private final IArticleService articleService;

    @Priv(type = MemberUserType.TYPE)
    @GetMapping("/contribute")
    public void accountContribute(@RequestParam Long sid,
                                  @RequestParam String pp,
                                  @RequestParam(value = "cid", required = false) Long contentId,
                                  @RequestParam(required = false, defaultValue = "false") Boolean preview,
                                  HttpServletResponse response)
            throws IOException {
        response.setCharacterEncoding(Charset.defaultCharset().displayName());
        response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

        CmsSite site = this.siteService.getSite(sid);
        if (Objects.isNull(site)) {
            this.catchException("/", response, new RuntimeException("Site not found: " + sid));
            return;
        }
        long memberId = StpMemberUtil.getLoginIdAsLong();
        Member member = this.memberService.getById(memberId);
        if (Objects.isNull(member)) {
            this.catchException(SiteUtils.getSiteLink(site, pp, preview), response, new RuntimeException("Member not found: " + memberId));
            return;
        }
        // 查找动态模板
        final String detailTemplate = PublishPipeProp_MemberContributeTemplate.getValue(pp, site.getPublishPipeProps());
        File templateFile = this.templateService.findTemplateFile(site, detailTemplate, pp);
        if (Objects.isNull(templateFile) || !templateFile.exists()) {
            this.catchException(SiteUtils.getSiteLink(site, pp, preview), response, new RuntimeException("Template not found: " + detailTemplate));
            return;
        }
        long s = System.currentTimeMillis();
        // 生成静态页面
        try {
            // 模板ID = 通道:站点目录:模板文件名
            String templateKey = SiteUtils.getTemplateKey(site, pp, detailTemplate);
            TemplateContext templateContext = new TemplateContext(templateKey, preview, pp);
            // init template datamode
            TemplateUtils.initGlobalVariables(site, templateContext);
            // init templateType data to datamode
            templateContext.getVariables().put("Member", member);
            templateContext.getVariables().put("MemberResourcePrefix", MemberUtils.getMemberResourcePrefix(preview));
            templateContext.getVariables().put("Request", ServletUtils.getParameters());
            if (IdUtils.validate(contentId)) {
                CmsContent content = this.contentService.getById(contentId);
                CmsArticleDetail articleDetail = this.articleService.getById(content.getContentId());
                ContributeArticleVO article = ContributeArticleVO.newInstance(content, articleDetail);
                if (StringUtils.isNotEmpty(content.getLogo())) {
                    article.setLogoSrc(InternalUrlUtils.getActualUrl(article.getLogo(), pp, preview));
                }
                article.setContentHtml(InternalUrlUtils.dealResourceInternalUrl(article.getContentHtml()));
                templateContext.getVariables().put("Article", article);
            } else {
                templateContext.getVariables().put("Article", new ContributeArticleVO());
            }
            // staticize
            this.staticizeService.process(templateContext, response.getWriter());
            log.debug("会员投稿页面模板解析：{}，耗时：{} ms", member.getMemberId(), System.currentTimeMillis() - s);
        } catch (Exception e) {
            this.catchException(SiteUtils.getSiteLink(site, pp, preview), response, e);
        }
    }

    private void catchException(String redirectLink, HttpServletResponse response, Exception e) throws IOException {
        if (log.isDebugEnabled()) {
            e.printStackTrace(response.getWriter());
        } else {
            response.sendRedirect(redirectLink); // TODO 通过发布通道属性配置错误页面
        }
    }
}

