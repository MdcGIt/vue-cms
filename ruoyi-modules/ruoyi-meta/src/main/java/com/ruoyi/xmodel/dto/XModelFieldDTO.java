package com.ruoyi.xmodel.dto;

import jakarta.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.xmodel.domain.XModelField;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XModelFieldDTO extends BaseDTO {
	
	private Long fieldId;

    private Long modelId;

    @NotNull
    private String name;

    @NotNull
    private String code;

    private String fieldType;

    private String fieldName;

    @NotNull
    private String controlType;

    @NotNull
    private String mandatoryFlag;

    private FieldOptions options;
    
    private String defaultValue;
    
    private boolean isDefaultTable;
    
	public static XModelFieldDTO newInstance(XModelField modelField) {
		XModelFieldDTO dto = new XModelFieldDTO();
    	BeanUtils.copyProperties(modelField, dto);
		return dto;
	}
}
