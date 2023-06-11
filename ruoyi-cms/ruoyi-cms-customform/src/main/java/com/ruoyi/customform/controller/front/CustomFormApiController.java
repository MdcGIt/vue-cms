package com.ruoyi.customform.controller.front;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.customform.domain.CmsCustomForm;
import com.ruoyi.customform.domain.dto.CustomFormAddDTO;
import com.ruoyi.customform.domain.dto.CustomFormEditDTO;
import com.ruoyi.customform.permission.CustomFormPriv;
import com.ruoyi.customform.service.ICustomFormService;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.security.StpAdminUtil;
import com.ruoyi.xmodel.service.IModelDataService;
import com.ruoyi.xmodel.service.IModelService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 自定义表单API控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/api/customform")
@RequiredArgsConstructor
public class CustomFormApiController extends BaseRestController {

	private final ICustomFormService customFormService;

	private final IModelDataService modelDataService;

	@PostMapping("/submit")
	public R<?> submitForm(@RequestBody @Validated Map<String, Object> formData) {
		Long formId = MapUtils.getLong(formData, "formId");
		if (!IdUtils.validate(formId)) {
			return R.fail("Unknown form: " + formId);
		}
		CmsCustomForm form = this.customFormService.getById(formId);
		if (Objects.isNull(form)) {
			return R.fail("Unknown form: " + formId);
		}
		// TODO 校验提交规则：验证码，IP，浏览器指纹，会员登录

		this.modelDataService.saveModelData(form.getModelId(), IdUtils.getSnowflakeIdStr(), formData);
		return R.ok();
	}
}
