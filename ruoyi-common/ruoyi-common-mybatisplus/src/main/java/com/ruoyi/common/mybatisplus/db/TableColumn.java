package com.ruoyi.common.mybatisplus.db;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableColumn {

	/**
	 * 主键字段标识
	 */
	public static final String COLUMN_KEY = "PRI";

	/**
	 * YES
	 */
	public static final String YES = "YES";
	
	/**
	 * NO
	 */
	public static final String NO = "NO";

	/**
	 * 字段名称
	 */
	private String columnName;

	/**
	 * 字段类型
	 */
	private String columnType;

	/**
	 * 是否可为空
	 */
	private String isNullable;

	/**
	 * 主键标识
	 */
	private String columnKey;
	
	/**
	 * 是否自增
	 */
	private String autoIncrement;
	
	public TableColumn(String columnName, String columnType, String isNullable, String columnKey, String autoIncrement) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.isNullable = isNullable;
		this.columnKey = columnKey;
		this.autoIncrement = autoIncrement;
	}

	public boolean isPrimary() {
		return COLUMN_KEY.equals(this.columnKey);
	}

	public boolean notNull() {
		return NO.equals(this.isNullable);
	}
}
