package com.ruoyi.xmodel.db;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DbTableColumn {
	
	private String tableSchema;
	
	private String tableName;
	
	private String columnName;
	
	private long ordinalPosition;
	
	private String columnDefault;
	
	private String isNullable;
	
	private boolean isPrimaryKey;
	
	private String dataType;
	
	private long characterMaximumLength;
	
	private long characterOctetLength;
	
	private long numeric_precision;
	
	private long numeric_scale;
	
	private String columnType;
	
	private String columnComment;

	public String isMandatory() {
		return "NO".equalsIgnoreCase(this.isNullable) ? "Y" : "N";
	}
	
}
