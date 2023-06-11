package com.ruoyi.customform.domain.dto;

import com.ruoyi.common.security.domain.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 自定义表单添加DTO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CustomFormAddDTO extends BaseDTO {

    /**
     * 站点ID
     */
    private Long siteId;

    /**
     * 名称
     */
    @NotBlank
    private String name;

    /**
     * 编码
     */
    @NotBlank
    private String code;

    /**
     * 模型数据表
     */
    @NotBlank
    private String tableName;

    /**
     * 备注
     */
    private String remark;
}
