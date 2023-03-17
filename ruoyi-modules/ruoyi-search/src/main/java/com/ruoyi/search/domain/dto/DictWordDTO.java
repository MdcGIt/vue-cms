package com.ruoyi.search.domain.dto;

import java.util.List;

import com.ruoyi.common.security.domain.BaseDTO;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DictWordDTO extends BaseDTO {
	
	@NotEmpty
	private String wordType;

	/**
     * 搜索词
     */
	@NotEmpty
    private List<String> words;
}