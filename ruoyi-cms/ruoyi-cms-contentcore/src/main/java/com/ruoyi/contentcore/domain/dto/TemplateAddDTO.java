package com.ruoyi.contentcore.domain.dto;

import com.ruoyi.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateAddDTO extends BaseDTO {
    
    private Long siteId;
    
    @NotEmpty
    private String publishPipeCode;

    @NotEmpty
    private String path;
    
    private String remark;
}
