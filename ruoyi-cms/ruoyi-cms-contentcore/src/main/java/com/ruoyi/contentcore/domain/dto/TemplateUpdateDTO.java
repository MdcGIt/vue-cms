package com.ruoyi.contentcore.domain.dto;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.system.validator.LongId;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateUpdateDTO extends BaseDTO {

	@LongId
	private Long templateId;

    private String content;
    
    private String remark;
}
