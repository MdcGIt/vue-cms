package com.ruoyi.common.mybatisplus;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BackupEntity {

	private String backupId;
	
	private String backupOperator;
	
	private LocalDateTime backupTime;
}
