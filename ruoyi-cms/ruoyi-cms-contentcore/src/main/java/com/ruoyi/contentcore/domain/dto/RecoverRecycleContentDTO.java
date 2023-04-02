package com.ruoyi.contentcore.domain.dto;

import java.util.List;

import com.ruoyi.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecoverRecycleContentDTO extends BaseDTO {

	public Long catalogId;
	
	@NotEmpty
	public List<Long> backupIds;
}
