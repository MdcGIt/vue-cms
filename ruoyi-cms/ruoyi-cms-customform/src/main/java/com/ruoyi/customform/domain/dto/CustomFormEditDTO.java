package com.ruoyi.customform.domain.dto;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.system.validator.LongId;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 自定义表单编辑DTO
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Getter
@Setter
public class CustomFormEditDTO extends BaseDTO {

    @LongId
    private Long formId;

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
     * 模板配置
     */
    private List<Map<String, String>> templates;

    /**
     * 备注
     */
    private String remark;
}
