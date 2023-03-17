package com.ruoyi.xmodel.db;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DbTable {
	
	private String tableSchema;
	
	private String tableName;
	
	private String tableComment;
	
	private String createTime;
	
	private String updateTime;
	
	private List<DbTableColumn> columns;
}
