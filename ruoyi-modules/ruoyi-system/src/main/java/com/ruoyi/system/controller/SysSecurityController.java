package com.ruoyi.system.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
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
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.system.domain.SysSecurityConfig;
import com.ruoyi.system.fixed.dict.PasswordRule;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.service.ISecurityConfigService;

import lombok.RequiredArgsConstructor;

/**
 * 安全配置控制器
 * 
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/system/security/config")
public class SysSecurityController extends BaseRestController {

	private final ISecurityConfigService securityConfigService;
	
	@SaAdminCheckLogin
	@GetMapping
	public R<?> listConfigs() {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<SysSecurityConfig> q = new LambdaQueryWrapper<SysSecurityConfig>()
				.orderByDesc(SysSecurityConfig::getConfigId);
		Page<SysSecurityConfig> page = securityConfigService.page(new Page<>(pr.getPageNumber(), pr.getPageSize()), q);
		return bindDataTable(page);
	}

	@SaAdminCheckLogin
	@GetMapping("/{id}")
	public R<?> getConfig(@PathVariable Long id) {
		SysSecurityConfig securityConfig = securityConfigService.getById(id);
		Assert.notNull(securityConfig, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(id));
		return R.ok(securityConfig);
	}

	@SaAdminCheckLogin
	@GetMapping("/current")
	public R<?> getCurrentConfig() {
		SysSecurityConfig securityConfig = securityConfigService.getSecurityConfig();
		Assert.notNull(securityConfig, CommonErrorCode.DATA_NOT_FOUND::exception);
		
		String ruleRegex = PasswordRule.getRuleRegex(securityConfig.getPasswordRule());
		securityConfig.setPasswordRulePattern(ruleRegex);
		return R.ok(securityConfig);
	}

	@SaAdminCheckLogin
	@Log(title = "安全配置", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addConfig(@RequestBody SysSecurityConfig config) {
		config.setOperator(StpAdminUtil.getLoginUser());
		this.securityConfigService.addConfig(config);
		return R.ok();
	}

	@SaAdminCheckLogin
	@Log(title = "安全配置", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> saveConfig(@RequestBody SysSecurityConfig config) {
		config.setOperator(StpAdminUtil.getLoginUser());
		this.securityConfigService.saveConfig(config);
		return R.ok();
	}

	@SaAdminCheckLogin
	@Log(title = "安全配置", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> delConfig(@RequestBody List<Long> configIds) {
		this.securityConfigService.deleteConfigs(configIds);
		return R.ok();
	}

	@SaAdminCheckLogin
	@Log(title = "安全配置", businessType = BusinessType.UPDATE)
	@PutMapping("/changeStatus/{id}")
	public R<?> changeConfigStatus(@PathVariable Long id) {
		this.securityConfigService.changeConfigStatus(id);
		return R.ok();
	}
}
