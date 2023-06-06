package com.ruoyi.exmodel.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.exmodel.permission.EXModelPriv;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.SaAdminCheckLogin;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.system.validator.LongId;
import com.ruoyi.xmodel.domain.XModel;
import com.ruoyi.xmodel.domain.XModelField;
import com.ruoyi.xmodel.dto.XModelDTO;
import com.ruoyi.xmodel.dto.XModelFieldDTO;
import com.ruoyi.xmodel.dto.XModelFieldDataDTO;
import com.ruoyi.xmodel.service.IModelFieldService;
import com.ruoyi.xmodel.service.IModelService;

import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 扩展模型前端控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/exmodel")
@RequiredArgsConstructor
public class EXModelController extends BaseRestController {

	public final static String OwnerType_Site = "site";

	private final IModelService modelService;

	private final IModelFieldService modelFieldService;

	private final ISiteService siteService;

	@Priv(type = AdminUserType.TYPE, value = EXModelPriv.View)
	@GetMapping
	public R<?> getModelList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		LambdaQueryWrapper<XModel> q = new LambdaQueryWrapper<XModel>()
				.eq(XModel::getOwnerType, OwnerType_Site)
				.eq(XModel::getOwnerId, site.getSiteId()).
				like(StringUtils.isNotEmpty(query), XModel::getName, query);
		Page<XModel> page = this.modelService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}

	@Log(title = "新增扩展模型", businessType = BusinessType.INSERT)
	@Priv(type = AdminUserType.TYPE, value = EXModelPriv.Add)
	@PostMapping
	public R<?> add(@RequestBody @Validated XModelDTO dto) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		dto.setOwnerType(OwnerType_Site);
		dto.setOwnerId(String.valueOf(site.getSiteId()));
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.modelService.addModel(dto);
		return R.ok();
	}

	@Log(title = "编辑扩展模板", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = { EXModelPriv.Add, EXModelPriv.Edit })
	@PutMapping
	public R<?> edit(@RequestBody @Validated XModelDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.modelService.editModel(dto);
		return R.ok();
	}

	@Log(title = "删除扩展模型", businessType = BusinessType.DELETE)
	@Priv(type = AdminUserType.TYPE, value = EXModelPriv.Delete)
	@DeleteMapping
	public R<?> remove(@RequestBody @Validated @NotEmpty List<XModelDTO> dtoList) {
		if (dtoList == null || dtoList.size() == 0) {
			return R.fail("参数不能为空");
		}
		List<Long> modelIds = dtoList.stream().map(XModelDTO::getModelId).toList();
		this.modelService.deleteModel(modelIds);
		return R.ok();
	}

	@Priv(type = AdminUserType.TYPE, value = EXModelPriv.View)
	@GetMapping("/fields")
	public R<?> getModelFieldList(@RequestParam(value = "query", required = false) String query,
			@RequestParam(value = "modelId", required = false) Long modelId) {
		PageRequest pr = this.getPageRequest();
		LambdaQueryWrapper<XModelField> q = new LambdaQueryWrapper<XModelField>().eq(XModelField::getModelId, modelId)
				.like(StringUtils.isNotEmpty(query), XModelField::getName, query);
		Page<XModelField> page = this.modelFieldService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}

	@Log(title = "新增扩展模型字段", businessType = BusinessType.INSERT)
	@Priv(type = AdminUserType.TYPE, value = { EXModelPriv.Add, EXModelPriv.Edit })
	@PostMapping("/field")
	public R<?> addField(@RequestBody @Validated XModelFieldDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.modelFieldService.addModelField(dto);
		return R.ok();
	}

	@Log(title = "编辑扩展模型字段", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = { EXModelPriv.Add, EXModelPriv.Edit })
	@PutMapping("/field")
	public R<?> editField(@RequestBody @Validated XModelFieldDTO dto) {
		dto.setOperator(StpAdminUtil.getLoginUser());
		this.modelFieldService.editModelField(dto);
		return R.ok();
	}

	@Log(title = "删除扩展模型字段", businessType = BusinessType.UPDATE)
	@Priv(type = AdminUserType.TYPE, value = { EXModelPriv.Add, EXModelPriv.Edit })
	@DeleteMapping("/field")
	public R<?> removeField(@RequestBody @NotEmpty List<Long> fieldIds) {
		this.modelFieldService.deleteModelField(fieldIds);
		return R.ok();
	}

	@SaAdminCheckLogin
	@GetMapping("data/{modelId}")
	public R<?> getModelData(@PathVariable @LongId Long modelId, @RequestParam String pkValue) {
		List<XModelFieldDataDTO> mfd = this.modelFieldService.getFieldDatas(modelId, pkValue);
		return R.ok(mfd);
	}
}
