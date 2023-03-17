package com.ruoyi.system.domain.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRoleDTO {
	
	@Min(1)
	private Long userId;
	
	@NotNull
	private List<Long> roleIds;
}

