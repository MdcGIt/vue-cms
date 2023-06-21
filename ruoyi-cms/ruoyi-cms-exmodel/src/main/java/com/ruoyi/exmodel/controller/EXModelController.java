package com.ruoyi.exmodel.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ruoyi.exmodel.CmsExtendMetaModelType;
import com.ruoyi.exmodel.domain.CmsExtendModelData;
import com.ruoyi.xmodel.core.impl.MetaControlType_Checkbox;
import com.ruoyi.xmodel.service.IModelDataService;
import com.ruoyi.xmodel.util.XModelUtils;
import org.apache.commons.collections4.MapUtils;
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
import com.ruoyi.exmodel.domain.dto.XModelFieldDataDTO;
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

	private final IModelService modelService;

	private final IModelFieldService modelFieldService;

	private final IModelDataService modelDataService;

	private final ISiteService siteService;

	@Priv(type = AdminUserType.TYPE, value = EXModelPriv.View)
	@GetMapping
	public R<?> getModelList(@RequestParam(value = "query", required = false) String query) {
		PageRequest pr = this.getPageRequest();
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		LambdaQueryWrapper<XModel> q = new LambdaQueryWrapper<XModel>()
				.eq(XModel::getOwnerType, CmsExtendMetaModelType.TYPE)
				.eq(XModel::getOwnerId, site.getSiteId())
				.like(StringUtils.isNotEmpty(query), XModel::getName, query);
		Page<XModel> page = this.modelService.page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true), q);
		return this.bindDataTable(page);
	}

	@Log(title = "新增扩展模型", businessType = BusinessType.INSERT)
	@Priv(type = AdminUserType.TYPE, value = EXModelPriv.Add)
	@PostMapping
	public R<?> add(@RequestBody @Validated XModelDTO dto) {
		CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
		dto.setOwnerType(CmsExtendMetaModelType.TYPE);
		dto.setOwnerId(site.getSiteId().toString());
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

	@SaAdminCheckLogin
	@GetMapping("/data")
	public R<?> getModelData(@RequestParam @LongId Long modelId,
							 @RequestParam String dataType,
							 @RequestParam(required = false, defaultValue = "") String dataId) {
		Map<String, Object> data = this.modelDataService.getModelDataByPkValue(modelId,
				Map.of(
						CmsExtendMetaModelType.FIELD_MODEL_ID.getCode(), modelId,
						CmsExtendMetaModelType.FIELD_DATA_TYPE.getCode(), dataType,
						CmsExtendMetaModelType.FIELD_DATA_ID.getCode(), dataId
				));

		List<XModelFieldDataDTO> list = new ArrayList<>();
		List<XModelField> fields = this.modelFieldService.lambdaQuery().eq(XModelField::getModelId, modelId).list();
		for (XModelField f : fields) {
			String fv = MapUtils.getString(data, f.getCode(), f.getDefaultValue());
			XModelFieldDataDTO dto = XModelFieldDataDTO.newInstance(f, fv);
			dto.setOptions(XModelUtils.getOptions(f.getOptions()));
			if (MetaControlType_Checkbox.TYPE.equals(dto.getControlType())) {
				if (dto.getValue() == null || StringUtils.isBlank(dto.getValue().toString())) {
					dto.setValue(new String[0]);
				} else {
					String[] value = StringUtils.split(dto.getValue().toString(), StringUtils.COMMA);
					dto.setValue(value);
				}
			}
			list.add(dto);
		}
		return R.ok(list);
	}
}
