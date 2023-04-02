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

	private String columnName;

	private String columnType;

	private String isNullable;

	private String columnKey;
	
	public TableColumn(String columnName, String columnType, String isNullable, String columnKey) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.isNullable = isNullable;
		this.columnKey = columnKey;
	}

	public boolean isPrimary() {
		return COLUMN_KEY.equals(this.columnKey);
	}

	public boolean notNull() {
		return NO.equals(this.isNullable);
	}
}
