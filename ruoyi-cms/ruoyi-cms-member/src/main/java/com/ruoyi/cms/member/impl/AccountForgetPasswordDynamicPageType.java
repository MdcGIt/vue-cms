package com.ruoyi.cms.member.impl;

import com.ruoyi.cms.member.publishpipe.PublishPipeProp_MemberForgetPasswordTemplate;
import com.ruoyi.contentcore.core.IDynamicPageType;
import org.springframework.stereotype.Component;

/**
 * 会员找回密码页
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IDynamicPageType.BEAN_PREFIX + AccountForgetPasswordDynamicPageType.TYPE)
public class AccountForgetPasswordDynamicPageType implements IDynamicPageType {

    public static final String TYPE = "AccountForgetPassword";

    public static final String REQUEST_PATH = "account/forget_password";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "会员找回密码页";
    }

    @Override
    public String getRequestPath() {
        return REQUEST_PATH;
    }

    @Override
    public String getPublishPipeKey() {
        return PublishPipeProp_MemberForgetPasswordTemplate.KEY;
    }
}
