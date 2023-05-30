package com.ruoyi.system.permission;

import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.NotPermissionException;
import com.ruoyi.common.security.domain.LoginUser;
import com.ruoyi.common.utils.Assert;

/**
 * 权限工具类
 *
 * @author 兮玥
 * @email liweiyimwz@126.com
 */
public class PermissionUtils {

    /**
     * 校验权限
     *
     * @param perm
     * @param loginUser
     * @return
     */
    public static void checkPermission(String perm, LoginUser loginUser) {
        Assert.isTrue(loginUser.hasPermission(perm),
                () -> new NotPermissionException(perm, loginUser.getUserType())
                        .setCode(SaErrorCode.CODE_11051));
    }
}
