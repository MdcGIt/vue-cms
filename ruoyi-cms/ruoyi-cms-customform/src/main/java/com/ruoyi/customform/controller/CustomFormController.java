package com.ruoyi.customform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.exception.CommonErrorCode;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.Assert;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.IPublishPipeService;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.customform.domain.CmsCustomForm;
import com.ruoyi.customform.domain.dto.CustomFormAddDTO;
import com.ruoyi.customform.domain.dto.CustomFormEditDTO;
import com.ruoyi.customform.domain.vo.CustomFormVO;
import com.ruoyi.customform.permission.CustomFormPriv;
import com.ruoyi.customform.service.ICustomFormService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 自定义表单控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/customform")
@RequiredArgsConstructor
public class CustomFormController extends BaseRestController {

    private final ICustomFormService customFormService;

    private final ISiteService siteService;

    private final IPublishPipeService publishPipeService;

    private final IModelService modelService;

    @Priv(type = AdminUserType.TYPE, value = CustomFormPriv.View)
    @GetMapping
    public R<?> getList(@RequestParam(value = "query", required = false) String query) {
        PageRequest pr = this.getPageRequest();
        CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
        Page<CmsCustomForm> page = this.customFormService.lambdaQuery()
                .like(StringUtils.isNotEmpty(query), CmsCustomForm::getName, query)
                .page(new Page<>(pr.getPageNumber(), pr.getPageSize(), true));
        return this.bindDataTable(page);
    }

    @Priv(type = AdminUserType.TYPE, value = CustomFormPriv.View)
    @GetMapping("/{formId}")
    public R<?> getDetail(@PathVariable @LongId Long formId) {
        CmsCustomForm form = this.customFormService.getById(formId);
        Assert.notNull(form, () -> CommonErrorCode.DATA_NOT_FOUND_BY_ID.exception("formId", formId));

        CustomFormVO vo = CustomFormVO.from(form);
        List<Map<String, String>> templates = this.publishPipeService.getPublishPipes(form.getSiteId()).stream().map(pp -> {
            return Map.of("name", pp.getName(),
                    "code", pp.getCode(),
                    "template", form.getTemplates().getOrDefault(pp.getCode(), ""));
        }).toList();
        vo.setTemplates(templates);
        return R.ok(vo);
    }

    @Log(title = "新增自定义表单", businessType = BusinessType.INSERT)
    @Priv(type = AdminUserType.TYPE, value = CustomFormPriv.Add)
    @PostMapping
    public R<?> add(@RequestBody @Validated CustomFormAddDTO dto) {
        CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
        dto.setOperator(StpAdminUtil.getLoginUser());
        dto.setSiteId(site.getSiteId());
        this.customFormService.addCustomForm(dto);
        return R.ok();
    }

    @Log(title = "编辑自定义表单", businessType = BusinessType.UPDATE)
    @Priv(type = AdminUserType.TYPE, value = {CustomFormPriv.Add, CustomFormPriv.Edit})
    @PutMapping
    public R<?> edit(@RequestBody @Validated CustomFormEditDTO dto) {
        dto.setOperator(StpAdminUtil.getLoginUser());
        this.customFormService.editCustomForm(dto);
        return R.ok();
    }

    @Log(title = "删除自定义表单", businessType = BusinessType.DELETE)
    @Priv(type = AdminUserType.TYPE, value = CustomFormPriv.Delete)
    @DeleteMapping
    public R<?> remove(@RequestBody @Validated @NotEmpty List<Long> formIds) {
        this.customFormService.deleteCustomForm(formIds);
        return R.ok();
    }

    @Log(title = "发布自定义表单", businessType = BusinessType.UPDATE)
    @Priv(type = AdminUserType.TYPE, value = { CustomFormPriv.Add, CustomFormPriv.Edit })
    @PostMapping("/publish")
    public R<?> publish(@RequestBody @Validated @NotEmpty List<Long> formIds) {
        this.customFormService.publishCustomForms(formIds, StpAdminUtil.getLoginUser().getUsername());
        return R.ok();
    }
}
