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
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.fixed.FixedConfigUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;
import com.ruoyi.system.permission.SysMenuPriv;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISysConfigService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * 参数配置 信息操作处理
 * 
 * @author ruoyi
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/config")
public class SysConfigController extends BaseRestController {
	
	private final ISysConfigService configService;

	/**
	 * 获取参数配置列表
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigList)
	@GetMapping("/list")
	public R<?> list(SysConfig config) {
		PageRequest pr = this.getPageRequest();
		Page<SysConfig> page = this.configService.lambdaQuery()
				.like(StringUtils.isNotEmpty(config.getConfigKey()), SysConfig::getConfigKey, config.getConfigKey())
				.like(StringUtils.isNotEmpty(config.getConfigName()), SysConfig::getConfigName, config.getConfigName())
				.orderByDesc(SysConfig::getConfigKey)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize()));
		page.getRecords().forEach(conf -> {
			conf.setFixed(FixedConfigUtils.isFixedConfig(conf.getConfigKey()) ? YesOrNo.YES : YesOrNo.NO);
		});
		I18nUtils.replaceI18nFields(page.getRecords());
		return bindDataTable(page);
	}

	@Log(title = "参数管理", businessType = BusinessType.EXPORT)
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigExport)
	@PostMapping("/export")
	public void export(HttpServletResponse response, SysConfig config) {
		LambdaQueryWrapper<SysConfig> q = new LambdaQueryWrapper<SysConfig>()
				.like(StringUtils.isNotEmpty(config.getConfigKey()), SysConfig::getConfigKey, config.getConfigKey())
				.like(StringUtils.isNotEmpty(config.getConfigName()), SysConfig::getConfigName, config.getConfigName())
				.orderByDesc(SysConfig::getConfigKey);
		List<SysConfig> list = configService.list(q);
		I18nUtils.replaceI18nFields(list);
		this.exportExcel(list, SysConfig.class, response);
	}

	/**
	 * 根据参数编号获取详细信息
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigList)
	@GetMapping(value = "/{configId}")
	public R<?> getInfo(@PathVariable Long configId) {
		SysConfig config = this.configService.getById(configId);
		I18nUtils.replaceI18nFields(config);
		return R.ok(config);
	}

	/**
	 * 根据参数键名查询参数值
	 */
	@SaAdminCheckLogin
	@GetMapping(value = "/configKey/{configKey}")
	public R<?> getConfigKey(@PathVariable String configKey) {
		return R.ok(configService.selectConfigByKey(configKey));
	}

	/**
	 * 新增参数配置
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigAdd)
	@Log(title = "参数管理", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@Validated @RequestBody SysConfig config) {
		config.setCreateBy(StpAdminUtil.getLoginUser().getUsername());
		configService.insertConfig(config);
		return R.ok();
	}

	/**
	 * 修改参数配置
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigEdit)
	@Log(title = "参数管理", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@Validated @RequestBody SysConfig config) {
		config.setUpdateBy(StpAdminUtil.getLoginUser().getUsername());
		configService.updateConfig(config);
		return R.ok();
	}

	/**
	 * 删除参数配置
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigRemove)
	@Log(title = "参数管理", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> configIds) {
		Assert.isTrue(IdUtils.validate(configIds), () -> CommonErrorCode.INVALID_REQUEST_ARG.exception());
		configService.deleteConfigByIds(configIds);
		return R.ok();
	}

	/**
	 * 刷新参数缓存
	 */
	@Priv(type = AdminUserType.TYPE, value = SysMenuPriv.SysConfigRemove)
	@Log(title = "参数管理", businessType = BusinessType.CLEAN)
	@DeleteMapping("/refreshCache")
	public R<?> refreshCache() {
		configService.resetConfigCache();
		return R.ok();
	}
}
