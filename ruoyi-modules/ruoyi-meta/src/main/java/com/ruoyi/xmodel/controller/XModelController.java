package com.ruoyi.xmodel.controller;

import java.util.List;
import java.util.Map;

import com.ruoyi.common.i18n.I18nUtils;
import com.ruoyi.xmodel.core.IMetaControlType;
import com.ruoyi.xmodel.core.IMetaModelType;
import com.ruoyi.xmodel.util.XModelUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.validator.LongId;
import com.ruoyi.xmodel.domain.XModel;
import com.ruoyi.xmodel.dto.XModelDTO;
import com.ruoyi.xmodel.service.IModelService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 元数据模型前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/xmodel")
public class XModelController extends BaseRestController {

	private final IModelService modelService;

	private final List<IMetaControlType> controlTypes;

	@GetMapping("/controls")
	public R<?> getControlTypeOptions() {
		List<Map<String, String>> list = controlTypes.stream().map(t ->
			Map.of("id", t.getType(), "name", I18nUtils.get(t.getName()))
		).toList();
		return R.ok(list);
	}

	@GetMapping
	public R<?> getModelList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<XModel> page = this.modelService.lambdaQuery().like(StringUtils.isNotEmpty(query), XModel::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@GetMapping("/tables")
	public R<?> getModelDataTableList(@RequestParam String type) {
		List<String> list = this.modelService.listModelDataTables(type);
		return this.bindDataTable(list);
	}

	@GetMapping("/tableFields")
	public R<?> getModelTableFields(@RequestParam("modelId") @LongId Long modelId) {
		XModel model = this.modelService.getById(modelId);
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", modelId));

		List<String> list = this.modelService.listModelTableFields(model);
		return this.bindDataTable(list);
	}

	@GetMapping("/{modelId}")
	public R<?> getModel(@PathVariable @LongId Long modelId) {
		XModel model = this.modelService.getById(modelId);
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", modelId));

		IMetaModelType mmt = XModelUtils.getMetaModelType(model.getOwnerType());
		model.setIsDefaultTable(mmt.getDefaultTable().equals(model.getTableName()));
		return R.ok(model);
	}

	@Log(title = "新增元数据", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@RequestBody @Validated XModelDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.modelService.addModel(dto);
		return R.ok();
	}

	@Log(title = "编辑元数据", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@RequestBody @Validated XModelDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.modelService.editModel(dto);
		return R.ok();
	}

	@Log(title = "删除元数据", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<XModelDTO> dtoList) {
		List<Long> modelIds = dtoList.stream().map(XModelDTO::getModelId).toList();
		this.modelService.deleteModel(modelIds);
		return R.ok();
	}
}
