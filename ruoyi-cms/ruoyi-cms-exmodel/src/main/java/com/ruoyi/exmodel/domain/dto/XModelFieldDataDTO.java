package com.ruoyi.exmodel.domain.dto;

import java.util.List;
import java.util.Map;

import com.ruoyi.exmodel.CmsExtendMetaModelType;
import com.ruoyi.xmodel.util.XModelUtils;
import com.ruoyi.xmodel.domain.XModelField;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XModelFieldDataDTO {

    private String label;
    
    private String fieldName;
    	
    private String controlType;
    
    private List<Map<String, String>> options;
    
    private Object value;
    
	public static XModelFieldDataDTO newInstance(XModelField field, String value) {
		XModelFieldDataDTO dto = new XModelFieldDataDTO();
		dto.setLabel(field.getName());
		dto.setFieldName(CmsExtendMetaModelType.DATA_FIELD_PREFIX + field.getCode());
		dto.setControlType(field.getControlType());
		dto.setValue(value);
		return dto;
	}
}
