package com.ruoyi.contentcore.domain.dto;

import org.springframework.web.multipart.MultipartFile;

import com.ruoyi.common.security.domain.BaseDTO;
import com.ruoyi.contentcore.domain.CmsSite;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ResourceUploadDTO extends BaseDTO {

	private CmsSite site;

	private MultipartFile file;
	
	private String name;
	
	private String remark;
	
}
