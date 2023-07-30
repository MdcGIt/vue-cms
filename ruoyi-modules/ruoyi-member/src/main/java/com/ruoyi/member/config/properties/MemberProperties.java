package com.ruoyi.member.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ruoyi.member")
public class MemberProperties {

    /**
     * 会员头像等资源上传目录
     */
    private String uploadPath;
}
