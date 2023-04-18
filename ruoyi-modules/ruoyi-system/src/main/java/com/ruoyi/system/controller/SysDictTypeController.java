package com.ruoyi.system.controller;

import java.util.List;

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
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysDictType;
import com.ruoyi.system.fixed.FixedDictUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;
import com.ruoyi.system.permission.SysMenuPriv;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysDictTypeService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * 数据字典信息
 * 
 * @author ruoyi
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/dict/type")
public class SysDictTypeController extends BaseRestController {
	
	private final ISysDictTypeService dictTypeService;
	
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictList)
	@GetMapping("/list")
	public R<?> list(SysDictType dictType) {
		PageRequest pr = this.getPageRequest();
		Page<SysDictType> page = dictTypeService.lambdaQuery()
				.like(StringUtils.isNotEmpty(dictType.getDictName()), SysDictType::getDictName, dictType.getDictName())
				.like(StringUtils.isNotEmpty(dictType.getDictType()), SysDictType::getDictType, dictType.getDictType())
				.orderByDesc(SysDictType::getDictType)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
		page.getRecords().forEach(dt -> {
			dt.setFixed(FixedDictUtils.isFixedDictType(dt.getDictType()) ? YesOrNo.YES : YesOrNo.NO);
		});
		return bindDataTable(page);
	}

	@Log(title = "字典类型", businessType = BusinessType.EXPORT)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictExport)
	@PostMapping("/export")
	public void export(HttpServletResponse response, SysDictType dictType) {
		List<SysDictType> list = dictTypeService.lambdaQuery()
				.like(StringUtils.isNotEmpty(dictType.getDictName()), SysDictType::getDictName, dictType.getDictName())
				.like(StringUtils.isNotEmpty(dictType.getDictType()), SysDictType::getDictType, dictType.getDictType())
				.orderByDesc(SysDictType::getDictType)
				.list();
		this.exportExcel(list, SysDictType.class, response);
	}

	/**
	 * 查询字典类型详细
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictList)
	@GetMapping(value = "/{dictId}")
	public R<?> getInfo(@PathVariable Long dictId) {
		return R.ok(dictTypeService.getById(dictId));
	}

	/**
	 * 新增字典类型
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictAdd)
	@Log(title = "字典类型", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysDictType dict) {
		dict.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		dictTypeService.insertDictType(dict);
		return R.ok();
	}

	/**
	 * 修改字典类型
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictEdit)
	@Log(title = "字典类型", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysDictType dict) {
		dict.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		dictTypeService.updateDictType(dict);
		return R.ok();
	}

	/**
	 * 删除字典类型
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictRemove)
	@Log(title = "字典类型", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> dictIds) {
		dictTypeService.deleteDictTypeByIds(dictIds);
		return R.ok();
	}

	/**
	 * 刷新字典缓存
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysDictRemove)
	@Log(title = "字典类型", businessType = BusinessType.CLEAN)
	@DeleteMapping("/refreshCache")
	public R<?> refreshCache() {
		dictTypeService.resetDictCache();
		return R.ok();
	}

	/**
	 * 获取字典选择框列表
	 */
	@SaAdminCheckLogin
	@GetMapping("/optionselect")
	public R<?> optionselect() {
		List<SysDictType> dictTypes = dictTypeService.list();
		return R.ok(dictTypes);
	}
}
