package com.ruoyi.cms.member.controller.front;

import com.ruoyi.common.security.anno.Priv;
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
import com.ruoyi.member.domain.Member;
import com.ruoyi.member.domain.vo.MemberCache;
import com.ruoyi.member.security.MemberUserType;
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
			this.catchException(response, new RuntimeException("Site not found: " + siteId));
			return;
		}
		MemberCache member = this.memberStatDataService.getMemberCache(memberId);
		if (Objects.isNull(member)) {
			this.catchException(response, new RuntimeException("Member not found: " + memberId));
			return;
		}
		// 查找动态模板
		final String detailTemplate = "account/account_centre.template.html"; // TODO 站点动态模板配置
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
			this.catchException(response, e);
		}
	}

	@Priv(type = MemberUserType.TYPE)
	@GetMapping("/{memberId}/setting")
	public void accountSetting(@PathVariable("memberId") @LongId Long memberId,
							   @RequestParam Long sid,
							   @RequestParam String pp,
							   @RequestParam(required = false, defaultValue = "false") Boolean preview,
							   HttpServletResponse response)
			throws IOException {
		response.setCharacterEncoding(Charset.defaultCharset().displayName());
		response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

		CmsSite site = this.siteService.getSite(sid);
		if (Objects.isNull(site)) {
			this.catchException(response, new RuntimeException("Site not found: " + sid));
			return;
		}
		Member member = this.memberService.getById(memberId);
		if (Objects.isNull(member)) {
			this.catchException(response, new RuntimeException("Member not found: " + memberId));
			return;
		}
		// 查找动态模板
		final String detailTemplate = "account/account_setting.template.html";
		File templateFile = this.templateService.findTemplateFile(site, detailTemplate, pp);
		if (Objects.isNull(templateFile) || !templateFile.exists()) {
			this.catchException(response, new RuntimeException("Template not found: " + detailTemplate));
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
			this.catchException(response, e);
		}
	}

	@Priv(type = MemberUserType.TYPE)
	@GetMapping("/{memberId}/password")
	public void accountPassword(@PathVariable("memberId") @LongId Long memberId,
								@RequestParam Long sid,
								@RequestParam String pp,
								@RequestParam(required = false, defaultValue = "false") Boolean preview,
								HttpServletResponse response)
			throws IOException {
		response.setCharacterEncoding(Charset.defaultCharset().displayName());
		response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

		CmsSite site = this.siteService.getSite(sid);
		if (Objects.isNull(site)) {
			this.catchException(response, new RuntimeException("Site not found: " + sid));
			return;
		}
		Member member = this.memberService.getById(memberId);
		if (Objects.isNull(member)) {
			this.catchException(response, new RuntimeException("Member not found: " + memberId));
			return;
		}
		// 查找动态模板
		final String detailTemplate = "account/account_password.template.html";
		File templateFile = this.templateService.findTemplateFile(site, detailTemplate, pp);
		if (Objects.isNull(templateFile) || !templateFile.exists()) {
			this.catchException(response, new RuntimeException("Template not found: " + detailTemplate));
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
			this.catchException(response, e);
		}
	}

	@Priv(type = MemberUserType.TYPE)
	@GetMapping("/{memberId}/change_email")
	public void accountChangeEmail(@PathVariable("memberId") @LongId Long memberId,
								   @RequestParam Long sid,
								   @RequestParam String pp,
								   @RequestParam(required = false, defaultValue = "false") Boolean preview,
								   HttpServletResponse response)
			throws IOException {
		response.setCharacterEncoding(Charset.defaultCharset().displayName());
		response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

		CmsSite site = this.siteService.getSite(sid);
		if (Objects.isNull(site)) {
			this.catchException(response, new RuntimeException("Site not found: " + sid));
			return;
		}
		Member member = this.memberService.getById(memberId);
		if (Objects.isNull(member)) {
			this.catchException(response, new RuntimeException("Member not found: " + memberId));
			return;
		}
		// 查找动态模板
		final String detailTemplate = "account/account_change_email.template.html";
		File templateFile = this.templateService.findTemplateFile(site, detailTemplate, pp);
		if (Objects.isNull(templateFile) || !templateFile.exists()) {
			this.catchException(response, new RuntimeException("Template not found: " + detailTemplate));
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
			this.catchException(response, e);
		}
	}

	@Priv(type = MemberUserType.TYPE)
	@GetMapping("/{memberId}/contribute")
	public void accountContribute(@PathVariable("memberId") @LongId Long memberId,
								   @RequestParam Long sid,
								   @RequestParam String pp,
								   @RequestParam(required = false, defaultValue = "false") Boolean preview,
								   HttpServletResponse response)
			throws IOException {
		response.setCharacterEncoding(Charset.defaultCharset().displayName());
		response.setContentType("text/html; charset=" + Charset.defaultCharset().displayName());

		CmsSite site = this.siteService.getSite(sid);
		if (Objects.isNull(site)) {
			this.catchException(response, new RuntimeException("Site not found: " + sid));
			return;
		}
		Member member = this.memberService.getById(memberId);
		if (Objects.isNull(member)) {
			this.catchException(response, new RuntimeException("Member not found: " + memberId));
			return;
		}
		// 查找动态模板
		final String detailTemplate = "account/account_contribute.template.html";
		File templateFile = this.templateService.findTemplateFile(site, detailTemplate, pp);
		if (Objects.isNull(templateFile) || !templateFile.exists()) {
			this.catchException(response, new RuntimeException("Template not found: " + detailTemplate));
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
			log.debug("会员投稿页面模板解析：{}，耗时：{} ms", member.getMemberId(), System.currentTimeMillis() - s);
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

