package com.ruoyi.member.controller;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.member.domain.MemberLevelConfig;
import com.ruoyi.member.domain.dto.LevelConfigDTO;
import com.ruoyi.member.domain.vo.LevelTypeVO;
import com.ruoyi.member.level.ILevelType;
import com.ruoyi.member.permission.MemberPriv;
import com.ruoyi.member.service.IMemberLevelConfigService;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

@Priv(type = AdminUserType.TYPE, value = MemberPriv.MemberLevel)
@RequiredArgsConstructor
@RestController
@RequestMapping("/member/levelConfig")
public class MemberLevelConfigController extends BaseRestController {

	private final IMemberLevelConfigService memberLevelConfigService;

	@GetMapping
	public R<?> getPageList(@RequestParam(value = "levelType", required = false) String levelType,
			@RequestParam(value = "level", required = false) Integer level) {
		PageRequest pr = this.getPageRequest();
		Page<MemberLevelConfig> page = this.memberLevelConfigService.lambdaQuery()
				.eq(StringUtils.isNotEmpty(levelType), MemberLevelConfig::getLevelType, levelType)
				.eq(Objects.nonNull(level), MemberLevelConfig::getLevel, level)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		page.getRecords().forEach(conf -> {
			ILevelType lt = this.memberLevelConfigService.getLevelType(conf.getLevelType());
			conf.setLevelTypeName(I18nUtils.get(lt.getName()));
		});
		return this.bindDataTable(page);
	}

	@GetMapping("/{configId}")
	public R<?> getLevelConfigDetail(@PathVariable("configId") @Min(1) Long configId) {
		MemberLevelConfig lvConfig = this.memberLevelConfigService.getById(configId);
		Assert.notNull(lvConfig, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("id", configId));
		return R.ok(lvConfig);
	}

	@GetMapping("/types")
	public R<?> getLevelTypes() {
		List<LevelTypeVO> levelTypes = this.memberLevelConfigService.getLevelTypes().values().stream()
				.map(lt -> new LevelTypeVO(lt.getId(), I18nUtils.get(lt.getName()))).toList();
		return R.ok(levelTypes);
	}

	@Log(title = "新增会员等级配置", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addMemberConfig(@RequestBody LevelConfigDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.memberLevelConfigService.addLevelConfig(dto);
		return R.ok();
	}

	@Log(title = "编辑会员等级配置", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> updateMemberConfig(@RequestBody LevelConfigDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.memberLevelConfigService.updateLevelConfig(dto);
		return R.ok();
	}

	@Log(title = "删除会员等级配置", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> deleteConfig(@RequestBody @NotEmpty List<Long> configIds) {
		this.memberLevelConfigService.deleteLevelConfig(configIds);
		return R.ok();
	}
}