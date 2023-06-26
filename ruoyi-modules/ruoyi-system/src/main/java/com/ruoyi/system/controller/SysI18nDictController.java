package com.ruoyi.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.config.I18nMessageSource;
import com.ruoyi.system.domain.SysDictData;
import com.ruoyi.system.domain.SysI18nDict;
import com.ruoyi.system.fixed.dict.I18nDictType;
import com.ruoyi.system.permission.SysMenuPriv;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.service.ISysDictTypeService;
import com.ruoyi.system.service.ISysI18nDictService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 国际化配置控制器
 * 
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/i18n/dict")
public class SysI18nDictController extends BaseRestController {
	
	private final ISysI18nDictService i18nDictService;
	
	private final ISysDictTypeService dictTypeService;
	
	private final I18nMessageSource messageSource;

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictList)
	@GetMapping
	public R<?> list(SysI18nDict dict) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysI18nDict> q = new LambdaQueryWrapper<SysI18nDict>()
				.like(StringUtils.isNotEmpty(dict.getLangTag()), SysI18nDict::getLangTag, dict.getLangTag())
				.like(StringUtils.isNotEmpty(dict.getLangKey()), SysI18nDict::getLangKey, dict.getLangKey())
				.like(StringUtils.isNotEmpty(dict.getLangValue()), SysI18nDict::getLangValue, dict.getLangValue())
				.orderByDesc(SysI18nDict::getLangTag);
		Page<SysI18nDict> page = i18nDictService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	@Log(title = "国际化管理", businessType = BusinessType.EXPORT)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictExport)
	@PostMapping("/export")
	public void export(HttpServletResponse response, SysI18nDict dict) {
		LambdaQueryWrapper<SysI18nDict> q = new LambdaQueryWrapper<SysI18nDict>()
				.like(StringUtils.isNotEmpty(dict.getLangTag()), SysI18nDict::getLangTag, dict.getLangTag())
				.like(StringUtils.isNotEmpty(dict.getLangKey()), SysI18nDict::getLangKey, dict.getLangKey())
				.like(StringUtils.isNotEmpty(dict.getLangValue()), SysI18nDict::getLangValue, dict.getLangValue())
				.orderByDesc(SysI18nDict::getLangTag);
		List<SysI18nDict> list = i18nDictService.list(q);
		this.exportExcel(list, SysI18nDict.class, response);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictList)
	@GetMapping(value = "/{i18nDictId}")
	public R<?> getInfo(@PathVariable Long i18nDictId) {
		return R.ok(this.i18nDictService.getById(i18nDictId));
	}

	@Priv(type = AdminUserType.TYPE)
	@GetMapping(value = "/langKey/{langKey}")
	public R<?> listByLangKey(@PathVariable @NotEmpty String langKey) {
		LambdaQueryWrapper<SysI18nDict> q = new LambdaQueryWrapper<SysI18nDict>()
				.eq(SysI18nDict::getLangKey, langKey);
		List<SysI18nDict> list = i18nDictService.list(q);

		List<SysDictData> datas = dictTypeService.selectDictDatasByType(I18nDictType.TYPE);
		for (SysDictData data : datas) {
			Optional<SysI18nDict> opt = list.stream().filter(d -> d.getLangTag().equals(data.getDictValue())).findFirst();
			if (!opt.isPresent()) {
				SysI18nDict d = new SysI18nDict();
				d.setLangTag(data.getDictValue());
				d.setLangKey(langKey);
				d.setLangValue(I18nUtils.get(langKey, Locale.forLanguageTag(data.getDictValue())));
				list.add(d);
			}
		}
		return R.ok(list);
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictAdd)
	@Log(title = "国际化管理", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysI18nDict config) {
		i18nDictService.insertI18nDict(config);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictEdit)
	@Log(title = "国际化管理", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysI18nDict dict) {
		i18nDictService.updateI18nDict(dict);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictEdit)
	@Log(title = "国际化管理", businessType = BusinessType.UPDATE)
	@PutMapping("/batch")
	public R<?> batchSave(@RequestBody @NotEmpty @Validated List<SysI18nDict> i18nDicts) {
		i18nDictService.batchSaveI18nDicts(i18nDicts);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictRemove)
	@Log(title = "国际化管理", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> i18nDictIds) {
		Assert.notEmpty(i18nDictIds, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception());
		i18nDictService.deleteI18nDictByIds(i18nDictIds);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysI18NDictRemove)
	@Log(title = "国际化管理", businessType = BusinessType.CLEAN)
	@DeleteMapping("/refreshCache")
	public R<?> refreshCache() throws IOException {
		i18nDictService.loadMessages(this.messageSource);
		return R.ok();
	}
}
