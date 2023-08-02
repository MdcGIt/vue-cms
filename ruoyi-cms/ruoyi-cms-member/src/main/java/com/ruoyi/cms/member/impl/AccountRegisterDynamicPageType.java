package com.ruoyi.cms.member.impl;

import com.ruoyi.cms.member.publishpipe.PublishPipeProp_MemberRegisterTemplate;
import com.ruoyi.contentcore.core.IDynamicPageType;
import org.springframework.stereotype.Component;

/**
 * 会员注册页
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Component(IDynamicPageType.BEAN_PREFIX + AccountRegisterDynamicPageType.TYPE)
public class AccountRegisterDynamicPageType implements IDynamicPageType {

    public static final String TYPE = "AccountRegister";

    public static final String REQUEST_PATH = "account/register";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getName() {
        return "会员注册页";
    }

    @Override
    public String getRequestPath() {
        return REQUEST_PATH;
    }

    @Override
    public String getPublishPipeKey() {
        return PublishPipeProp_MemberRegisterTemplate.KEY;
    }
}
