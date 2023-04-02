package com.ruoyi.contentcore.domain.vo;

import java.time.LocalDateTime;

import com.ruoyi.contentcore.domain.CmsContent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecycleContentVO extends CmsContent {
	
	private static final long serialVersionUID = 1L;

	private Long backupId;

	private String backupOperator;
	
	private LocalDateTime backupTime;
	
	private String backupRemark;
}
