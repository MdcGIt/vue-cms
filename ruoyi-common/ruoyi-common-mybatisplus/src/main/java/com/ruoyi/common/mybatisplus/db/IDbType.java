package com.ruoyi.common.mybatisplus.db;

public interface IDbType {
	
	static final String COLUMN_BACKUP_ID = "backup_id";

	static final String COLUMN_BACKUP_OPERATOR = "backup_operator";
	
	static final String COLUMN_BACKUP_TIME = "backup_time";
	
	static final String COLUMN_BACKUP_REMARK = "backup_remark";
	
	static String getBackupTableName(String sourceTableName) {
		return sourceTableName + "_backup";
	}

	/**
	 * 数据库类型
	 */
	String getType();

	/**
	 * 创建备份表
	 */
	void createBackupTable(String sourceTable);

	/**
	 * 备份数据
	 * 
	 * @param <T>
	 * @param entity
	 * @param backupId
	 * @param backupOperator
	 * @param backupRemark
	 */
	<T> void backup(T entity, Long backupId, String backupOperator, String backupRemark);

	/**
	 * 恢复备份数据
	 * 
	 * @param backupId
	 * @param entityClazz
	 */
	void recover(Long backupId, Class<?> entityClazz);
}
