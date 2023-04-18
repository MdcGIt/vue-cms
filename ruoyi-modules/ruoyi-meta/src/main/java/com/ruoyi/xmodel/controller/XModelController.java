package com.ruoyi.xmodel.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
 * 扩展模型前端控制器
 * </p>
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/xmodel")
public class XModelController extends BaseRestController {

	private final IModelService modelService;

	@GetMapping
	public R<?> getModelList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<XModel> page = this.modelService.lambdaQuery().like(StringUtils.isNotEmpty(query), XModel::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@GetMapping("/tables")
	public R<?> getModelDataCustomTableList() {
		List<String> list = this.modelService.listModelDataCustomTable();
		return this.bindDataTable(list);
	}

	@GetMapping("/tableFields")
	public R<?> getModelTableFields(@RequestParam("modelId") @LongId Long modelId) {
		XModel model = this.modelService.getById(modelId);
		Assert.notNull(model, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("modelId", modelId));

		List<String> list = this.modelService.listModelTableFields(model);
		return this.bindDataTable(list);
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
