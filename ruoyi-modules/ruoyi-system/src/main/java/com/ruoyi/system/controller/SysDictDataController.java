package com.ruoyi.system.controller;

import java.util.List;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysDictData;
import com.ruoyi.system.fixed.FixedDictUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;
import com.ruoyi.system.permission.SysMenuPriv;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysDictDataService;
import com.ruoyi.system.service.ISysDictTypeService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 数据字典信息
 * 
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dict/data")
public class SysDictDataController extends BaseRestController {

	private final ISysDictDataService dictDataService;

	private final ISysDictTypeService dictTypeService;

	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictList)
	@GetMapping("/list")
	public R<?> list(SysDictData dictData) {
		PageRequest pr = this.getPageRequest();
		Page<SysDictData> page = dictDataService.lambdaQuery()
				.like(StringUtils.isNotEmpty(dictData.getDictLabel()), SysDictData::getDictLabel,
						dictData.getDictLabel())
				.like(StringUtils.isNotEmpty(dictData.getDictType()), SysDictData::getDictType, dictData.getDictType())
				.orderByAsc(SysDictData::getDictSort)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
		page.getRecords().forEach(dt -> {
			dt.setFixed(FixedDictUtils.isFixedDictData(dt.getDictType(), dt.getDictValue()) ? YesOrNo.YES : YesOrNo.NO);
		});
		I18nUtils.replaceI18nFields(page.getRecords(), LocaleContextHolder.getLocale());
		return bindDataTable(page);
	}

	@Log(title = "字典数据", businessType = BusinessType.EXPORT)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictExport)
	@PostMapping("/export")
	public void export(HttpServletResponse response, SysDictData dictData) {
		List<SysDictData> list = dictDataService.lambdaQuery()
				.like(StringUtils.isNotEmpty(dictData.getDictLabel()), SysDictData::getDictLabel,
						dictData.getDictLabel())
				.like(StringUtils.isNotEmpty(dictData.getDictType()), SysDictData::getDictType, dictData.getDictType())
				.orderByDesc(SysDictData::getDictSort).list();
		this.exportExcel(list, SysDictData.class, response);
	}

	/**
	 * 查询字典数据详细
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictList)
	@GetMapping(value = "/{dictCode}")
	public R<?> getInfo(@PathVariable Long dictCode) {
		SysDictData data = dictDataService.getById(dictCode);
		return R.ok(data);
	}

	/**
	 * 根据字典类型查询字典数据信息
	 */
	@SaAdminCheckLogin
	@GetMapping(value = "/type/{dictType}")
	public R<?> getDictDatasByType(@PathVariable String dictType) {
		List<SysDictData> datas = dictTypeService.selectDictDatasByType(dictType);
		I18nUtils.replaceI18nFields(datas);
		return R.ok(datas);
	}

	/**
	 * 新增字典类型
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictAdd)
	@Log(title = "字典数据", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysDictData dict) {
		dict.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		dictDataService.insertDictData(dict);
		return R.ok();
	}

	/**
	 * 修改保存字典类型
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictEdit)
	@Log(title = "字典数据", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysDictData dict) {
		dict.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		dictDataService.updateDictData(dict);
		return R.ok();
	}

	/**
	 * 删除字典类型
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictRemove)
	@Log(title = "字典类型", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody List<Long> dictCodes) {
		boolean validate = IdUtils.validate(dictCodes, true);
		Assert.isTrue(validate, () -> CommonErrorCode.INVALID_REQUEST_ARG.exception());
		dictDataService.deleteDictDataByIds(dictCodes);
		return R.ok();
	}
}
