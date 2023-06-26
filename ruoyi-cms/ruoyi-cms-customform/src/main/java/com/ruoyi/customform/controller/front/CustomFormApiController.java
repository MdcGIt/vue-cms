package com.ruoyi.customform.controller.front;

import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.customform.CmsCustomFormMetaModelType;
import com.ruoyi.customform.domain.CmsCustomForm;
import com.ruoyi.customform.service.ICustomFormService;
import com.ruoyi.member.security.StpMemberUtil;
import com.ruoyi.system.annotation.IgnoreDemoMode;
import com.ruoyi.system.fixed.dict.YesOrNo;
import com.ruoyi.xmodel.service.IModelDataService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
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

	@IgnoreDemoMode
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
		if (YesOrNo.isYes(form.getNeedLogin()) && !StpMemberUtil.isLogin()) {
			return R.fail("Please login first.");
		}
		// TODO 限制规则校验：验证码，IP，浏览器指纹
		if (YesOrNo.isYes(form.getNeedCaptcha())) {

		}
		String uuid = MapUtils.getString(formData, "uuid", StringUtils.EMPTY);
		String clientIp = ServletUtils.getIpAddr(ServletUtils.getRequest());

		formData.put(CmsCustomFormMetaModelType.FIELD_DATA_ID.getCode(), IdUtils.getSnowflakeId());
		formData.put(CmsCustomFormMetaModelType.FIELD_MODEL_ID.getCode(), form.getFormId());
		formData.put(CmsCustomFormMetaModelType.FIELD_SITE_ID.getCode(), form.getSiteId());
		formData.put(CmsCustomFormMetaModelType.FIELD_CLIENT_IP.getCode(), clientIp);
		formData.put(CmsCustomFormMetaModelType.FIELD_UUID.getCode(), uuid);
		formData.put(CmsCustomFormMetaModelType.FIELD_CREATE_TIME.getCode(), LocalDateTime.now());

		this.modelDataService.saveModelData(form.getModelId(), formData);
		return R.ok();
	}
}
