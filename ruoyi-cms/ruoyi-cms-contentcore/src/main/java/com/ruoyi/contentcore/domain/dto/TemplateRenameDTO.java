package com.ruoyi.contentcore.domain.dto;

import com.ruoyi.common.security.domain.BaseDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateRenameDTO extends BaseDTO {

	@NotNull
	@Min(1)
	private Long templateId;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "模板名称仅允许使用字母、数字和下划线")
    private String path;
    
    private String remark;
}
