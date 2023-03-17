package com.ruoyi.system.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.redis.RedisCache;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysSecurityConfig;
import com.ruoyi.system.domain.SysUser;
import com.ruoyi.system.exception.SysErrorCode;
import com.ruoyi.system.fixed.dict.EnableOrDisable;
import com.ruoyi.system.fixed.dict.PasswordRetryStrategy;
import com.ruoyi.system.fixed.dict.PasswordRule;
import com.ruoyi.system.fixed.dict.PasswordSensitive;
import com.ruoyi.system.fixed.dict.UserStatus;
import com.ruoyi.system.fixed.dict.YesOrNo;
import com.ruoyi.system.mapper.SysSecurityConfigMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.service.ISecurityConfigService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Service
@RequiredArgsConstructor
public class SecurityConfigServiceImpl extends ServiceImpl<SysSecurityConfigMapper, SysSecurityConfig>
		implements ISecurityConfigService {

	private final static String CACHE_KEY_CONFIG = "sys:security:config";

	private final static String CACHE_KEY_PWD_RETRY = "sys:security:pwdretry:";

	private final SysUserMapper userMapper;

	private final RedisCache redisCache;
	
	@Override
	public SysSecurityConfig getSecurityConfig() {
		SysSecurityConfig config = this.redisCache.getCacheObject(CACHE_KEY_CONFIG);
		if (Objects.isNull(config)) {
			config = this.getOne(new LambdaQueryWrapper<SysSecurityConfig>().eq(SysSecurityConfig::getStatus,
					EnableOrDisable.ENABLE));
			if (Objects.nonNull(config)) {
				this.redisCache.setCacheObject(CACHE_KEY_CONFIG, config);
			}
		}
		return config;
	}

	@Override
	public void addConfig(SysSecurityConfig config) {
		config.setStatus(EnableOrDisable.DISABLE); // 默认不开启
		config.createBy(config.getOperator().getUsername());
		this.save(config);
	}

	@Override
	public void saveConfig(SysSecurityConfig config) {
		SysSecurityConfig dbConfig = this.getById(config.getConfigId());
		Assert.notNull(dbConfig, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(config.getConfigId()));

		config.updateBy(config.getOperator().getUsername());
		this.updateById(config);
		this.redisCache.setCacheObject(CACHE_KEY_CONFIG, config);
	}

	@Override
	public void deleteConfigs(List<Long> configIds) {
		this.removeByIds(configIds);
		this.redisCache.deleteObject(CACHE_KEY_CONFIG);
	}

	@Override
	@Transactional
	public void changeConfigStatus(Long configId) {
		SysSecurityConfig config = this.getById(configId);
		Assert.notNull(config, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception(configId));

		if (!config.isEnable()) {
			// 未启用的安全规则，先禁用其他规则再设置规则开启
			List<SysSecurityConfig> list = this.list(new LambdaQueryWrapper<SysSecurityConfig>().ne(SysSecurityConfig::getConfigId, configId));
			list.forEach(c -> c.setStatus(EnableOrDisable.DISABLE));
			this.updateBatchById(list);
		}
		config.setStatus(config.isEnable() ? EnableOrDisable.DISABLE : EnableOrDisable.ENABLE);
		this.updateById(config);
	}

	@Override
	public void validPassword(SysUser user, String password) {
		SysSecurityConfig securityConfig = this.getSecurityConfig();
		if (Objects.nonNull(securityConfig) && securityConfig.isEnable()) {
			// 最大长度
			boolean valid = securityConfig.getPasswordLenMax() > 0
					&& password.length() <= securityConfig.getPasswordLenMax();
			Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
			// 最小长度
			valid = securityConfig.getPasswordLenMin() > 0 && password.length() >= securityConfig.getPasswordLenMin();
			Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
			// 校验规则检查
			valid = PasswordRule.match(securityConfig.getPasswordRule(), password);
			Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
			// 敏感字符检查
			valid = PasswordSensitive.check(securityConfig.getPasswordSensitive(), password, user);
			Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
			// 弱密码检查
			String[] weakPasswords = securityConfig.getWeakPasswords().split("\n");
			valid = StringUtils.isEmpty(weakPasswords) || !StringUtils.equalsAny(password, weakPasswords);
			Assert.isTrue(valid, SysErrorCode.INSECURE_PASSWORD::exception);
		}
	}

	@Override
	public void forceModifyPwdAfterResetPwd(SysUser user) {
		SysSecurityConfig securityConfig = this.getSecurityConfig();
		if (Objects.nonNull(securityConfig) && securityConfig.isEnable()
				&& securityConfig.checkForceModifyPwdAfterReset()) {
			user.setForceModifyPassword(YesOrNo.YES);
		}
	}

	@Override
	public void forceModifyPwdAfterUserAdd(SysUser user) {
		SysSecurityConfig securityConfig = this.getSecurityConfig();
		if (Objects.nonNull(securityConfig) && securityConfig.isEnable()
				&& securityConfig.checkForceModifyPwdAfterAdd()) {
			user.setForceModifyPassword(YesOrNo.YES);
		}
	}

	@Override
	public void onLoginPasswordError(SysUser user) {
		SysSecurityConfig config = this.getSecurityConfig();
		if (Objects.nonNull(config) && config.isEnable()) {
			// 缓存更新
			LoginPwdRetry lpe = this.redisCache.getCacheMapValue(CACHE_KEY_PWD_RETRY, user.getUserId().toString());
			if (Objects.isNull(lpe)) {
				lpe = new LoginPwdRetry(user.getUserId());
			}
			lpe.inc();
			this.redisCache.setCacheMapValue(CACHE_KEY_PWD_RETRY, user.getUserId().toString(), lpe);
			// 执行策略
			int passwordRetryLimit = config.getPasswordRetryLimit();
			if (passwordRetryLimit > 0 && lpe.getNum() >= passwordRetryLimit) {
				// 达到指定次数上限触发安全策略
				if (PasswordRetryStrategy.DISABLE.equals(config.getPasswordRetryStrategy())) {
					user.setStatus(UserStatus.DISABLE);
				} else if (PasswordRetryStrategy.LOCK.equals(config.getPasswordRetryStrategy())) {
					user.setStatus(UserStatus.LOCK);
					LocalDateTime lockEndTime = LocalDateTime.now().plusSeconds(config.getPasswordRetryLockSeconds());
					user.setLockEndTime(lockEndTime);
				}
				this.userMapper.updateById(user);
			}
		}
	}

	@Override
	public void onLoginSuccess(SysUser user) {
		this.redisCache.deleteCacheMapValue(CACHE_KEY_PWD_RETRY, user.getUserId().toString());
	}

	@Getter
	@Setter
	static class LoginPwdRetry {
		private Long uid;
		private Integer num = 0;
		private LocalDate date = LocalDate.now();

		public LoginPwdRetry() {
		}

		public LoginPwdRetry(Long uid) {
			this.uid = uid;
		}

		public void inc() {
			LocalDate now = LocalDate.now();
			if (!now.isEqual(this.date)) {
				this.num = 0;
			}
			this.date = now;
			this.num++;
		}
	}
}
