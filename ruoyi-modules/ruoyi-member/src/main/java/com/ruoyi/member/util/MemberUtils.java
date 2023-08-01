package com.ruoyi.member.util;

import com.ruoyi.member.config.MemberConfig;
import com.ruoyi.member.fixed.config.MemberResourcePrefix;
import com.ruoyi.system.fixed.config.BackendContext;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public class MemberUtils {

    public static String getMemberResourcePrefix(boolean isPreview) {
        if (isPreview) {
            return BackendContext.getValue() + MemberConfig.getResourcePrefix();
        }
        return MemberResourcePrefix.getValue();
    }
}
