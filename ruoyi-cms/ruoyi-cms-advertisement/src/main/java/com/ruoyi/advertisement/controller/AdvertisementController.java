package com.ruoyi.advertisement.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
import com.ruoyi.advertisement.IAdvertisementType;
import com.ruoyi.advertisement.domain.CmsAdvertisement;
import com.ruoyi.advertisement.pojo.dto.AdvertisementDTO;
import com.ruoyi.advertisement.pojo.vo.AdvertisementVO;
import com.ruoyi.advertisement.service.IAdvertisementService;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 广告前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/cms/advertisement")
public class AdvertisementController extends BaseRestController {

	private final IAdvertisementService advertisementService;

	@GetMapping("/types")
	public R<?> listAdvertisements() {
		List<Map<String, String>> list = advertisementService.getAdvertisementTypeList().stream()
				.map(t -> Map.of("id", t.getId(), "name", I18nUtils.get(t.getName()))).toList();
		return this.bindDataTable(list);
	}

	@GetMapping
	public R<?> listAdvertisements(@RequestParam(name = "adSpaceId") @Min(1) Long adSpaceId,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "state", required = false) Integer state) {
		PageRequest pr = getPageRequest();
		Page<CmsAdvertisement> page = this.advertisementService.lambdaQuery()
				.eq(CmsAdvertisement::getAdSpaceId, adSpaceId)
				.like(StringUtils.isNotEmpty(name), CmsAdvertisement::getName, name)
				.eq(state != null && state > -1, CmsAdvertisement::getState, state)
				.orderByDesc(CmsAdvertisement::getCreateTime).page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		page.getRecords().forEach(adv -> {
			IAdvertisementType advertisementType = this.advertisementService.getAdvertisementType(adv.getType());
			if (Objects.nonNull(advertisementType)) {
				adv.setTypeName(I18nUtils.get(advertisementType.getName()));
			}
		});
		return this.bindDataTable(page.getRecords(), (int) page.getTotal());
	}

	@GetMapping("/{advertisementId}")
	public R<AdvertisementVO> getAdvertisementInfo(@PathVariable("advertisementId") @Min(1) Long advertisementId) {
		CmsAdvertisement ad = this.advertisementService.getById(advertisementId);
		Assert.notNull(ad, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("advertisementId", advertisementId));
		return R.ok(new AdvertisementVO(ad).dealPreviewResourcePath());
	}

	@Log(title = "新增广告", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> addAdvertisement(@RequestBody AdvertisementDTO dto) throws IOException {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.advertisementService.addAdvertisement(dto);
		return R.ok();
	}

	@Log(title = "编辑广告", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> editAdvertisement(@RequestBody AdvertisementDTO dto) throws IOException {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.advertisementService.saveAdvertisement(dto);
		return R.ok();
	}

	@Log(title = "删除广告", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> deleteAdvertisements(@RequestBody List<Long> advertisementIds) {
		if (StringUtils.isEmpty(advertisementIds)) {
			return R.fail(StringUtils.messageFormat("参数[{0}]不能为空", "advertisementIds"));
		}
		this.advertisementService.deleteAdvertisement(advertisementIds);
		return R.ok();
	}

	@Log(title = "启用广告", businessType = BusinessType.UPDATE)
	@PutMapping("/enable")
	public R<?> enableAdvertisements(@RequestBody List<Long> advertisementIds) {
		if (StringUtils.isEmpty(advertisementIds)) {
			return R.fail(StringUtils.messageFormat("参数[{0}]不能为空", "advertisementIds"));
		}
		this.advertisementService.enableAdvertisement(advertisementIds,
				StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}

	@Log(title = "禁用广告", businessType = BusinessType.UPDATE)
	@PutMapping("/disable")
	public R<?> disableAdvertisements(@RequestBody List<Long> advertisementIds) {
		if (StringUtils.isEmpty(advertisementIds)) {
			return R.fail(StringUtils.messageFormat("参数[{0}]不能为空", "advertisementIds"));
		}
		this.advertisementService.disableAdvertisement(advertisementIds,
				StpAdminUtil.getLoginUser().getUsername());
		return R.ok();
	}
}
