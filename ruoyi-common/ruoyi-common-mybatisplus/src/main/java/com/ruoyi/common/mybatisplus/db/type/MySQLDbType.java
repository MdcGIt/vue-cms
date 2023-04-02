package com.ruoyi.common.mybatisplus.db.type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.ruoyi.common.mybatisplus.annotation.BackupTable;
import com.ruoyi.common.mybatisplus.db.IDbType;
import com.ruoyi.common.mybatisplus.db.TableColumn;
import com.ruoyi.common.mybatisplus.mapper.MySQLMapper;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.ReflectASMUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MySQLDbType implements IDbType {

	private static final List<TableColumn> BackupTableColumns_MySQL = List.of(
			new TableColumn(COLUMN_BACKUP_ID, "bigint", TableColumn.NO, TableColumn.COLUMN_KEY),
			new TableColumn(COLUMN_BACKUP_OPERATOR, "varchar(30)", TableColumn.NO, null),
			new TableColumn(COLUMN_BACKUP_TIME, "datetime", TableColumn.NO, null),
			new TableColumn(COLUMN_BACKUP_REMARK, "varchar(200)", TableColumn.YES, null));
	
	private final MySQLMapper mySQLMapper;

	@Override
	public String getType() {
		return DbType.MYSQL.getDb();
	}

	@Override
	public void createBackupTable(String sourceTable) {
		String backupTable = IDbType.getBackupTableName(sourceTable);
		Long tableExists = this.mySQLMapper.isTableExists(backupTable);
		if (tableExists > 0) {
			return; // 备份表已存在
		}
		// 原表字段
		List<TableColumn> tableColumns = this.mySQLMapper.selectTableColumns(sourceTable);
		// 添加备份表通用字段
		tableColumns.addAll(BackupTableColumns_MySQL);
		// 主键backup_id
		List<TableColumn> primaryColumns = BackupTableColumns_MySQL.stream().filter(TableColumn::isPrimary).toList();
		// 创建备份表
		this.mySQLMapper.createBackupTable(backupTable, tableColumns, primaryColumns);
	}
	
	@Override
	public void recover(Long backupId, Class<?> entityClazz) {
		BackupTable anno = entityClazz.getAnnotation(BackupTable.class);
		if (anno == null) {
			throw new RuntimeException("The entity '" + entityClazz.getName() + "' not annotationed by @BackupTable");
		}
		TableName annoTableName = entityClazz.getAnnotation(TableName.class);
		if (annoTableName == null) {
			throw new RuntimeException("The entity '" + entityClazz.getName() + "' not annotationed by @TableName");
		}
		Map<String, ColumnCache> sourceColumnMap = LambdaUtils.getColumnMap(entityClazz);
		String backupTableName = IDbType.getBackupTableName(annoTableName.value());
		
		List<String> columns = new ArrayList<>(sourceColumnMap.size());
		sourceColumnMap.values().forEach(columnCache -> {
			columns.add(columnCache.getColumn());
		});
		this.mySQLMapper.recoverBackup(backupTableName, annoTableName.value(), columns, backupId);
		
		this.mySQLMapper.deleteBackupById(backupTableName, backupId);
	}
	
	@Override
	public <T> void backup(T entity, Long backupId, String backupOperator, String backupRemark) {
		BackupTable anno = entity.getClass().getAnnotation(BackupTable.class);
		if (anno == null) {
			throw new RuntimeException("The entity '" + entity.getClass().getName() + "' not annotationed by @BackupTable");
		}
		TableName annoTableName = entity.getClass().getAnnotation(TableName.class);
		if (annoTableName == null) {
			throw new RuntimeException("The entity '" + entity.getClass().getName() + "' not annotationed by @TableName");
		}
		Map<String, ColumnCache> sourceColumnMap = LambdaUtils.getColumnMap(entity.getClass());
		String backupTableName = IDbType.getBackupTableName(annoTableName.value());
		
		List<TableColumn> backupColumns = this.mySQLMapper.selectTableColumns(backupTableName);
		List<String> columns = new ArrayList<>(backupColumns.size());
		List<Object> values = new ArrayList<>(backupColumns.size());
		for (TableColumn tableColumn : backupColumns) {
			String columnName = tableColumn.getColumnName();
			columns.add(columnName);
			if (COLUMN_BACKUP_OPERATOR.equals(columnName)) {
				values.add(backupOperator);
			} else if (COLUMN_BACKUP_TIME.equals(columnName)) {
				values.add(LocalDateTime.now());
			} else if (COLUMN_BACKUP_REMARK.equals(columnName)) {
				values.add(backupRemark);
			} else if (COLUMN_BACKUP_ID.equals(columnName)) {
				values.add(backupId);
			} else {
				String fieldName = com.baomidou.mybatisplus.core.toolkit.StringUtils.underlineToCamel(columnName);
				ColumnCache columnCache = sourceColumnMap.get(fieldName.toUpperCase());
				Object value = ReflectASMUtils.invokeGetter(entity, fieldName);
				if (Objects.nonNull(columnCache) && Objects.nonNull(columnCache.getMapping())) {
					value = JacksonUtils.to(value);
				}
				values.add(value);
			}
		}
		this.mySQLMapper.insertRow(backupTableName, columns, values);
	}
}
