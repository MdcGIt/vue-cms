package com.ruoyi.contentcore.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.staticize.func.IFunction;
import com.ruoyi.common.staticize.tag.ITag;
import com.ruoyi.contentcore.domain.vo.TemplateFuncVO;
import com.ruoyi.contentcore.domain.vo.TemplateTagVO;
import com.ruoyi.contentcore.perms.ContentCorePriv;
import com.ruoyi.system.security.AdminUserType;

import lombok.RequiredArgsConstructor;

/**
 * 静态化管理
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Priv(type = AdminUserType.TYPE, value = ContentCorePriv.StaticizeView)
@RestController
@RequiredArgsConstructor
@RequestMapping("/cms/staticize")
public class StaticizeController extends BaseRestController {

	private final List<ITag> tags;

	private final List<IFunction> functions;

	/**
	 * 获取静态化自定义模板标签列表
	 */
	@GetMapping("/tags")
	public R<?> getTemplateTags() {
		List<TemplateTagVO> list = this.tags.stream().map(tag -> {
			TemplateTagVO vo = TemplateTagVO.builder().name(I18nUtils.get(tag.getName())).tagName(tag.getTagName())
					.description(I18nUtils.get(tag.getDescription())).tagAttrs(tag.getTagAttrs()).build();
			vo.getTagAttrs().forEach(attr -> {
				attr.setName(I18nUtils.get(attr.getName()));
				attr.setUsage(I18nUtils.get(attr.getUsage()));
				attr.getOptions().forEach(op -> op.setDesc(I18nUtils.get(op.getDesc())));
			});
			return vo;
		}).toList();
		return R.ok(list);
	}

	/**
	 * 获取静态化自定义模板函数列表
	 */
	@GetMapping("/functions")
	public R<?> getTemplateFunctions() {
		List<TemplateFuncVO> list = this.functions.stream().map(func -> {
			TemplateFuncVO vo = TemplateFuncVO.builder().funcName(func.getFuncName())
					.desc(I18nUtils.get(func.getDesc())).funcArgs(func.getFuncArgs()).build();
			vo.getFuncArgs().forEach(arg -> {
				arg.setName(I18nUtils.get(arg.getName()));
				arg.setDesc(I18nUtils.get(arg.getDesc()));
			});
			return vo;
		}).toList();
		return R.ok(list);
	}
}
