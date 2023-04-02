package com.ruoyi.backup.core;

public interface IBackupType {

	public static final String BEAN_PREFIX = "BackupType_";
	
	/**
	 * 唯一标识
	 */
	public String getId();

	/**
	 * 名称
	 */
	public String getName();
}
