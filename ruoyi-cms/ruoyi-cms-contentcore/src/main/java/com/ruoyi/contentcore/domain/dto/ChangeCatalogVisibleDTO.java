package com.ruoyi.contentcore.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeCatalogVisibleDTO {

	@NotEmpty
	@Min(1)
	public Long catalogId;
	
	@NotEmpty
	public String visible;
}
