package com.ruoyi.link.domain.dto;

import org.springframework.beans.BeanUtils;

import com.ruoyi.link.domain.CmsLinkGroup;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LinkGroupDTO {
	
    private Long linkGroupId;

    private Long siteId;

    @NotNull
    private String name;

    @NotNull
    private String code;
    
    private Long sortFlag;
    
	public static LinkGroupDTO newInstance(CmsLinkGroup linkGroup) {
		LinkGroupDTO dto = new LinkGroupDTO();
    	BeanUtils.copyProperties(linkGroup, dto);
		return dto;
	}
}
