package com.ruoyi.search.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchLogDTO {

	/**
     * 搜索词
     */
	@NotBlank
    private String word;
    
    /**
     * Header:UserAgent
     */
    private String userAgent;
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * Header:Referer
     */
    private String referer;
}