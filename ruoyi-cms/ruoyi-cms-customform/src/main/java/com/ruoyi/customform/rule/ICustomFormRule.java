package com.ruoyi.customform.rule;

import com.ruoyi.customform.domain.CmsCustomForm;

/**
 * 自定义表单校验规则接口
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface ICustomFormRule {

    public boolean check(CmsCustomForm form);
}
