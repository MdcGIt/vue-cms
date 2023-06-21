package com.ruoyi.customform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ruoyi.common.domain.R;
import com.ruoyi.common.security.anno.Priv;
import com.ruoyi.common.security.web.BaseRestController;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.IdUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.service.ISiteService;
import com.ruoyi.customform.CmsCustomFormMetaModelType;
import com.ruoyi.customform.domain.CmsCustomFormData;
import com.ruoyi.customform.permission.CustomFormPriv;
import com.ruoyi.system.security.AdminUserType;
import com.ruoyi.system.validator.LongId;
import com.ruoyi.xmodel.service.IModelDataService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 自定义表单数据控制器
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@RestController
@RequestMapping("/cms/customform/data")
@RequiredArgsConstructor
public class CustomFormDataController extends BaseRestController {

    private final ISiteService siteService;

    private final IModelDataService modelDataService;

    @Priv(type = AdminUserType.TYPE, value = CustomFormPriv.View)
    @GetMapping
    public R<?> getList(@RequestParam @LongId Long formId, @RequestParam(required = false) String ip) {
        CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());
        PageRequest pr = this.getPageRequest();
        IPage<Map<String, Object>> page = this.modelDataService.selectModelDataPage(formId,
                new Page<>(pr.getPageNumber(), pr.getPageSize(), true), sqlBuilder -> {
            sqlBuilder.and().eq(CmsCustomFormMetaModelType.FIELD_SITE_ID.getFieldName(), site.getSiteId());
            if (StringUtils.isNotEmpty(ip)) {
                sqlBuilder.and().eq(CmsCustomFormMetaModelType.FIELD_CLIENT_IP.getFieldName(), ip);
            }
        });
        return this.bindDataTable(page);
    }

    @GetMapping("/detail")
    public R<?> getCustomFormDataDetail(@RequestParam @LongId Long formId, @RequestParam @LongId Long dataId) {
        Map<String, Object> dataMap = this.modelDataService.getModelDataByPkValue(formId,
                Map.of(CmsCustomFormMetaModelType.FIELD_DATA_ID.getCode(), dataId));
        return R.ok(dataMap);
    }

    @PostMapping
    public R<?> addCustomFormData(@RequestParam @LongId Long formId, @RequestBody Map<String, Object> data) {
        CmsSite site = this.siteService.getCurrentSite(ServletUtils.getRequest());

        data.put(CmsCustomFormMetaModelType.FIELD_DATA_ID.getCode(), IdUtils.getSnowflakeId());
        data.put(CmsCustomFormMetaModelType.FIELD_MODEL_ID.getCode(), formId);
        data.put(CmsCustomFormMetaModelType.FIELD_SITE_ID.getCode(), site.getSiteId());
        data.put(CmsCustomFormMetaModelType.FIELD_UUID.getCode(), StringUtils.EMPTY);
        data.put(CmsCustomFormMetaModelType.FIELD_CLIENT_IP.getCode(), "127.0.0.1");
        data.put(CmsCustomFormMetaModelType.FIELD_CREATE_TIME.getCode(), DateUtils.getDateTime());
        this.modelDataService.addModelData(formId, data);
        return R.ok();
    }

    @PutMapping
    public R<?> editCustomFormData(@RequestParam @LongId Long formId, @RequestBody Map<String, Object> data) {
        this.modelDataService.updateModelData(formId, data);
        return R.ok();
    }

    @DeleteMapping
    public R<?> deleteCustomFormDatas(@RequestParam @LongId Long formId, @RequestBody @NotEmpty List<Long> dataIds) {
        List<Map<String, String>> pkValues = dataIds.stream()
                .map(id -> Map.of(CmsCustomFormMetaModelType.FIELD_DATA_ID.getCode(), id.toString())).toList();
        this.modelDataService.deleteModelDataByPkValue(formId, pkValues);
        return R.ok();
    }
}
