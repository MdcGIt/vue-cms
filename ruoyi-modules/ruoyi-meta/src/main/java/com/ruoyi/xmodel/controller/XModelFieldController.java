package com.ruoyi.xmodel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.fixed.dict.YesOrNo;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.validator.LongId;
import com.ruoyi.xmodel.core.MetaModel;
import com.ruoyi.xmodel.core.MetaModelField;
import com.ruoyi.xmodel.domain.XModelField;
import com.ruoyi.xmodel.dto.XModelFieldDTO;
import com.ruoyi.xmodel.service.IModelFieldService;
import com.ruoyi.xmodel.service.IModelService;
import com.ruoyi.xmodel.util.XModelUtils;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 元数据模型字段前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@SaAdminCheckLogin
@RequiredArgsConstructor
@RestController
@RequestMapping("/xmodel/field")
public class XModelFieldController extends BaseRestController {

	private final IModelService modelService;

	private final IModelFieldService modelFieldService;

	@GetMapping("/list")
	public R<?> getModelFieldList(@RequestParam @LongId Long modelId,
								  @RequestParam(value = "query", required = false) String query) {
		PageRequest pr = getPageRequest();
		Page<XModelField> page = modelFieldService.lambdaQuery()
				.eq(XModelField::getModelId, modelId)
				.like(StringUtils.isNotEmpty(query), XModelField::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
	}

	@GetMapping("/all")
	public R<?> listAllModelFields(@RequestParam @LongId Long modelId,
								   @RequestParam(required = false, defaultValue = "true") Boolean ignoreFixedField) {
		MetaModel model = this.modelService.getMetaModel(modelId);
		List<MetaModelField> fields = new ArrayList<>();
		fields.addAll(model.getFields());
		if (!ignoreFixedField) {
			XModelUtils.getMetaModelType(model.getModel().getOwnerType()).getFixedFields().forEach(f -> {
				fields.add(f);
			});
		}
		return this.bindDataTable(fields);
	}

	@Log(title = "新增元数据字段", businessType = BusinessType.INSERT)
	@PostMapping
	public R<?> add(@RequestBody @Validated XModelFieldDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.modelFieldService.addModelField(dto);
		return R.ok();
	}

	@Log(title = "编辑元数据字段", businessType = BusinessType.UPDATE)
	@PutMapping
	public R<?> edit(@RequestBody @Validated XModelFieldDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.modelFieldService.editModelField(dto);
		return R.ok();
	}

	@Log(title = "删除元数据字段", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<Long> fieldIds) {
		this.modelFieldService.deleteModelField(fieldIds);
		return R.ok();
	}
}
