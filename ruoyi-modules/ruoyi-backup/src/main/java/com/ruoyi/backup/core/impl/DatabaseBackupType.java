package com.ruoyi.backup.core.impl;

import org.springframework.stereotype.Component;

import com.ruoyi.backup.core.IBackupType;

@Component
public class DatabaseBackupType implements IBackupType {
	
	public static final String ID = "database";

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "数据库备份";
	}
}
