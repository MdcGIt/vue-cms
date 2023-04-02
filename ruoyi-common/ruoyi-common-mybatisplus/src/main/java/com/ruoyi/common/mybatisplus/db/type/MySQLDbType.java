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
			new TableColumn(COLUMN_BACKUP_ID, "bigint", TableColumn.NO, TableColumn.COLUMN_KEY, TableColumn.YES),
			new TableColumn(COLUMN_BACKUP_OPERATOR, "varchar(30)", TableColumn.NO, null, TableColumn.NO),
			new TableColumn(COLUMN_BACKUP_TIME, "datetime", TableColumn.NO, null, TableColumn.NO),
			new TableColumn(COLUMN_BACKUP_REMARK, "varchar(200)", TableColumn.YES, null, TableColumn.NO));
	
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
	public void recover(Long backupId, Class<?> entityClass) {
		BackupTable anno = entityClass.getAnnotation(BackupTable.class);
		if (anno == null) {
			throw new RuntimeException("The entity '" + entityClass.getName() + "' not annotationed by @BackupTable");
		}
		TableName annoTableName = entityClass.getAnnotation(TableName.class);
		if (annoTableName == null) {
			throw new RuntimeException("The entity '" + entityClass.getName() + "' not annotationed by @TableName");
		}
		String sourceTableName = annoTableName.value();
		String backupTableName = IDbType.getBackupTableName(sourceTableName);
		
		Map<String, ColumnCache> sourceColumnMap = LambdaUtils.getColumnMap(entityClass);
		List<String> columns = new ArrayList<>();
		sourceColumnMap.values().forEach(columnCache -> {
			columns.add(columnCache.getColumn());
		});
		// 备份表数据插入原表
		this.mySQLMapper.recoverBackup(backupTableName, sourceTableName, columns, backupId);
		// 删除备份表数据
		this.mySQLMapper.deleteBackupById(backupTableName, backupId);
	}
	
	@Override
	public <T> void backup(T entity, String backupOperator, String backupRemark) {
		BackupTable anno = entity.getClass().getAnnotation(BackupTable.class);
		if (anno == null) {
			throw new RuntimeException("The entity '" + entity.getClass().getName() + "' not annotationed by @BackupTable");
		}
		TableName annoTableName = entity.getClass().getAnnotation(TableName.class);
		if (annoTableName == null) {
			throw new RuntimeException("The entity '" + entity.getClass().getName() + "' not annotationed by @TableName");
		}
		String backupTableName = IDbType.getBackupTableName(annoTableName.value());
		// 原表字段
		Map<String, ColumnCache> entityColumnMap = LambdaUtils.getColumnMap(entity.getClass());
		List<String> backupFields = new ArrayList<>();
		List<Object> backupFieldValues = new ArrayList<>();
		entityColumnMap.entrySet().forEach(e -> {
			ColumnCache columnCache = e.getValue();
			String entityFieldName = com.baomidou.mybatisplus.core.toolkit.StringUtils.underlineToCamel(columnCache.getColumn());
			Object fieldValue = ReflectASMUtils.invokeGetter(entity, entityFieldName);
			if (Objects.nonNull(columnCache) && Objects.nonNull(columnCache.getMapping())) {
				fieldValue = JacksonUtils.to(fieldValue);
			}
			backupFields.add(columnCache.getColumn());
			backupFieldValues.add(fieldValue);
		});
		// 备份表固定字段
		BackupTableColumns_MySQL.forEach(tc -> {
			if (COLUMN_BACKUP_OPERATOR.equals(tc.getColumnName())) {
				backupFields.add(tc.getColumnName());
				backupFieldValues.add(backupOperator);
			} else if (COLUMN_BACKUP_TIME.equals(tc.getColumnName())) {
				backupFields.add(tc.getColumnName());
				backupFieldValues.add(LocalDateTime.now());
			} else if (COLUMN_BACKUP_REMARK.equals(tc.getColumnName())) {
				backupFields.add(tc.getColumnName());
				backupFieldValues.add(backupRemark);
			}
		});
		this.mySQLMapper.insertRow(backupTableName, backupFields, backupFieldValues);
	}

	@Override
	public void deleteBackupByIds(List<Long> backupIds, Class<?> entityClass) {
		BackupTable anno = entityClass.getAnnotation(BackupTable.class);
		if (anno == null) {
			throw new RuntimeException("The entity '" + entityClass.getName() + "' not annotationed by @BackupTable");
		}
		TableName annoTableName = entityClass.getAnnotation(TableName.class);
		if (annoTableName == null) {
			throw new RuntimeException("The entity '" + entityClass.getName() + "' not annotationed by @TableName");
		}
		String backupTableName = IDbType.getBackupTableName(annoTableName.value());
		this.mySQLMapper.deleteBackupByIds(backupTableName, backupIds);
	}
}
