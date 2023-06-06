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
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.xmodel.domain.XModelField;
import com.ruoyi.xmodel.dto.XModelFieldDTO;
import com.ruoyi.xmodel.service.IModelFieldService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 扩展模型字段前端控制器
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

	private final IModelFieldService modelFieldService;

	@GetMapping
	public R<?> getModelList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		Page<XModelField> page = modelFieldService.lambdaQuery()
				.like(StringUtils.isNotEmpty(query), XModelField::getName, query)
				.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
		return this.bindDataTable(page);
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

	@Log(title = "删除原数据字段", businessType = BusinessType.DELETE)
	@DeleteMapping
	public R<?> remove(@RequestBody @NotEmpty List<XModelFieldDTO> dtoList) {
		List<Long> fieldIds = dtoList.stream().map(XModelFieldDTO::getFieldId).toList();
		this.modelFieldService.deleteModelField(fieldIds);
		return R.ok();
	}
}
