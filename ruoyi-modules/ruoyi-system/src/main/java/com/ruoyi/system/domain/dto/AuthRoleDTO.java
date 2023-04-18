package com.ruoyi.system.domain.dto;

import java.util.List;

import com.ruoyi.system.validator.LongId;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRoleDTO {
	
	@LongId
	private Long userId;
	
	@NotNull
	private List<Long> roleIds;
}

