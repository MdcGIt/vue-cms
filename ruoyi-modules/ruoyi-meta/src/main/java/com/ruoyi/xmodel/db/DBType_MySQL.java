package com.ruoyi.xmodel.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.ruoyi.common.utils.StringUtils;

public class DBType_MySQL {

	public final static String TYPE = "MySQL";
	
	final static String[] ColumnType_String = { "CHAR", "CHARSET", "TINYBLOB", "BLOB", "MEDIUMBLOB", "LONGBLOB", "TINYTEXT", "MEDIUMTEXT", "TEXT", "LONGTEXT"  };
	
	public String getTypeId() {
		return TYPE;
	}
	
	public String getCreateTableSql(DbTable dbTable) {
		return this.getCreateTableSql(dbTable, true, "InnoDB", "utf8mb4", "utf8mb4_general_ci", "Dynamic", "BTREE");
	}
	
	public String getCreateTableSql(DbTable dbTable, boolean dropExists, String engineType, String charset, String collate, String rowFormat, String primaryType) {
		StringBuilder sb = new StringBuilder();
		if (dropExists) {
			sb.append("DROP TABLE IF EXISTS `").append(dbTable.getTableName()).append("`;");
		}
		sb.append("CREATE TABLE `").append(dbTable.getTableName()).append("` (");
		List<String> primaryKeys = new ArrayList<>();
		dbTable.getColumns().forEach(col -> {
			sb.append("  `").append(col.getColumnName()).append("` ").append(col.getColumnType());
			if (ArrayUtils.contains(ColumnType_String, col.getDataType().toUpperCase())) {
				sb.append(" CHARACTER SET ").append(charset).append(" COLLATE ").append(collate);
			}
			if (col.getIsNullable().equals("NO")) {
				sb.append(" NOT NULL");
			}
			if (StringUtils.isNotEmpty(col.getColumnDefault())) {
				sb.append(" DEFAULT '").append(col.getColumnDefault()).append("'");
			}
			if (StringUtils.isNotEmpty(col.getColumnComment())) {
				sb.append(" COMMETN '").append(col.getColumnComment()).append("'");
			}
			sb.append(",");
			if (col.isPrimaryKey()) {
				primaryKeys.add(col.getColumnName());
			}
		});
		if (primaryKeys.size() > 0) {
			sb.append("PRIMARY KEY (`").append(StringUtils.join(primaryKeys, StringUtils.COMMA)).append("`) USING ").append(primaryType);
		} else {
			sb.deleteCharAt(sb.length());
		}
		sb.append(") ENGINE = ").append(engineType)
			.append(" CHARACTER SET = ").append(charset)
			.append(" COLLATE = ").append(collate)
			.append(" ROW_FORMAT = ").append(rowFormat);
		return sb.toString();
	}
	
	public String getAddColumnSql(String tableName, DbTableColumn column) {
		StringBuilder sb = new StringBuilder();
		sb.append("ALTER TABLE `").append(tableName).append("` ADD COLUMN `").append(column.getColumnName()).append("` ").append(column.getColumnType());
		if (column.getIsNullable().equalsIgnoreCase("NO")) {
			sb.append(" NOT NULL");
		}
		if (StringUtils.isNotEmpty(column.getColumnDefault())) {
			sb.append(" DEFAULT '").append(column.getColumnDefault()).append("'");
		}
		sb.append(";");
		return sb.toString();
	}
	
	public String getDropColumnSql(String tableName, DbTableColumn column) {
		StringBuilder sb = new StringBuilder();
		sb.append("ALTER TABLE `").append(tableName).append("` DROP COLUMN `").append(column.getColumnName()).append("`;");
		return sb.toString();
	}
}
