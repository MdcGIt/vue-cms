package com.ruoyi.common.mybatisplus.db.type;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.ruoyi.common.mybatisplus.db.DBTable;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.ruoyi.common.mybatisplus.annotation.BackupTable;
import com.ruoyi.common.mybatisplus.db.IDbType;
import com.ruoyi.common.mybatisplus.db.DBTableColumn;
import com.ruoyi.common.mybatisplus.mapper.MySQLMapper;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.ReflectASMUtils;

import lombok.RequiredArgsConstructor;

@Component(IDbType.BEAN_PREFIX + MySQLDbType.TYPE)
@RequiredArgsConstructor
public class MySQLDbType implements IDbType {

	public static final String TYPE = "mysql";

	private static final List<DBTableColumn> BackupTableColumns_MySQL = List.of(
			new DBTableColumn(COLUMN_BACKUP_ID, "bigint", null, false, true, true),
			new DBTableColumn(COLUMN_BACKUP_OPERATOR, "varchar(30)", null, false, false, false),
			new DBTableColumn(COLUMN_BACKUP_TIME, "datetime", null, false, false, false),
			new DBTableColumn(COLUMN_BACKUP_REMARK, "varchar(200)", null, true, false, false));

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
		List<DBTableColumn> tableColumns = this.listTableColumns(sourceTable);
		// 添加备份表通用字段
		tableColumns.addAll(BackupTableColumns_MySQL);
		// 主键backup_id
		List<DBTableColumn> primaryColumns = BackupTableColumns_MySQL.stream().filter(DBTableColumn::isPrimary).toList();
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
			if (COLUMN_BACKUP_OPERATOR.equals(tc.getName())) {
				backupFields.add(tc.getName());
				backupFieldValues.add(backupOperator);
			} else if (COLUMN_BACKUP_TIME.equals(tc.getName())) {
				backupFields.add(tc.getName());
				backupFieldValues.add(LocalDateTime.now());
			} else if (COLUMN_BACKUP_REMARK.equals(tc.getName())) {
				backupFields.add(tc.getName());
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

	@Override
	public List<DBTable> listTables(String tableName) {
		return this.mySQLMapper.listTables(tableName);
	}

	@Override
	public List<DBTableColumn> listTableColumns(String tableName) {
		return this.mySQLMapper.listTableColumns(tableName).stream().map(map -> {
			String columnName = MapUtils.getString(map, "column_name");
			String columnType = MapUtils.getString(map, "column_type");
			String columnDefault = MapUtils.getString(map, "column_default");
			String isNullable = MapUtils.getString(map, "is_nullable");
			String columnKey = MapUtils.getString(map, "column_key");
			String extra = MapUtils.getString(map, "extra");
			return new DBTableColumn(columnName, columnType, columnDefault, "YES".equals(isNullable),
					"PRI".equals(columnKey), "auto_increment".equals(extra));
		}).toList();
	}

	@Override
	public void dropTable(String tableName) {
		this.mySQLMapper.dropTable(tableName);
	}
}
