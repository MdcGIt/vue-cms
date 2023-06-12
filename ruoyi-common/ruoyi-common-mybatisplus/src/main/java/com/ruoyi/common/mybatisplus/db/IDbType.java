package com.ruoyi.common.mybatisplus.db;

import java.util.List;

public interface IDbType {

	String BEAN_PREFIX = "DB_TYPE_";
	
	String COLUMN_BACKUP_ID = "backup_id";

	String COLUMN_BACKUP_OPERATOR = "backup_operator";
	
	String COLUMN_BACKUP_TIME = "backup_time";
	
	String COLUMN_BACKUP_REMARK = "backup_remark";
	
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
	 * @param backupOperator
	 * @param backupRemark
	 */
	<T> void backup(T entity, String backupOperator, String backupRemark);

	/**
	 * 恢复备份数据
	 * 
	 * @param backupId
	 * @param entityClazz
	 */
	void recover(Long backupId, Class<?> entityClazz);

	/**
	 * 删除备份数据
	 * 
	 * @param backupIds
	 * @param entityClass
	 * @return
	 */
	void deleteBackupByIds(List<Long> backupIds, Class<?> entityClass);

	/**
	 * 根据表名前缀查询数据库所有数据表集合
	 *
	 * @param tableName
	 * @return
	 */
    List<DBTable> listTables(String tableName);

	List<DBTableColumn> listTableColumns(String tableName);

	void dropTable(String tableName);
}
