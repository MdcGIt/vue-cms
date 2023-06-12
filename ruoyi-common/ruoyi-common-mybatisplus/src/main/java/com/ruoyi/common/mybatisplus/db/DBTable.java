package com.ruoyi.common.mybatisplus.db;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DBTable {

	/**
	 * 数据库名
	 */
	private String tableSchema;

	/**
	 * 表名
	 */
	private String tableName;

	/**
	 * 备注
	 */
	private String tableComment;

	/**
	 * 创建时间
	 */
	private String createTime;

	/**
	 * 更新时间
	 */
	private String updateTime;

	/**
	 * 表字段
	 */
	private List<DBTableColumn> columns;
}
