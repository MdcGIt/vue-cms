package com.ruoyi.contentcore.domain.dto;

import com.ruoyi.system.validator.LongId;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeCatalogVisibleDTO {

	@LongId
	public Long catalogId;
	
	@NotEmpty
	public String visible;
}
