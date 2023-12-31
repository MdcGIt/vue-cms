package com.ruoyi.customform;

import com.ruoyi.common.utils.ReflectASMUtils;
import com.ruoyi.contentcore.domain.CmsSite;
import com.ruoyi.contentcore.fixed.config.SiteApiUrl;
import com.ruoyi.contentcore.properties.SiteApiUrlProperty;
import com.ruoyi.contentcore.util.SiteUtils;
import com.ruoyi.customform.domain.CmsCustomForm;

import java.util.Map;

/**
 * 自定义表单常量
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class CustomFormConsts {

    public static final String STATICIZE_DIRECTORY = "include/customform/";

    public static final String TemplateVariable_CustomForm = "CustomForm";

    public static String getCustomFormActionUrl(CmsSite site, String publishPipeCode) {
        return SiteApiUrlProperty.getValue(site, publishPipeCode) + "api/customform/submit";
    }

    public static Map<String, Object> getCustomFormVariables(CmsCustomForm form, CmsSite site, String publishPipeCode) {
        Map<String, Object> map = ReflectASMUtils.beanToMap(form);
        map.put("action", getCustomFormActionUrl(site, publishPipeCode));
        return map;
    }
}
