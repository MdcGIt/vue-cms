package com.ruoyi.system.domain.dto;

import com.ruoyi.common.security.domain.BaseDTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SysNoticeDTO extends BaseDTO {

	private Long noticeId;

	private String noticeTitle;

	private String noticeType;

	private String noticeContent;

	private String status;
	
	private String remark;
}
