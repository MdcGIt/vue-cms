package com.ruoyi.xmodel.dto;

import jakarta.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.xmodel.domain.XModel;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class XModelDTO extends BaseDTO {

    private Long modelId;
    
    private String ownerType;
    
    private String ownerId;
    
    @NotNull
    private String name;

    private String code;

    private String tableName;
    
	public static XModelDTO newInstance(XModel model) {
		XModelDTO dto = new XModelDTO();
    	BeanUtils.copyProperties(model, dto);
		return dto;
	}
}
