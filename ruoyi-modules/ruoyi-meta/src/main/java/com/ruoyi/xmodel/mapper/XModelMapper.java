package com.ruoyi.xmodel.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ruoyi.xmodel.db.DbTable;
import com.ruoyi.xmodel.db.DbTableColumn;
import com.ruoyi.xmodel.domain.XModel;

/**
 * <p>
 * 扩展模型Mapper 接口
 * </p>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface XModelMapper extends BaseMapper<XModel> {

	@Select("<script>"
			+ "select table_schema, table_name, table_comment, create_time, update_time"
			+ " from information_schema.tables"
			+ " where table_schema=(select database())"
			+ "<if test=\"tableName != null and tableName != ''\">"
			+ "	and lower(table_name) like lower(concat('%', #{tableName}, '%'))"
			+ "</if>"
			+ "</script>")
	public List<DbTable> listDbTable(@Param("tableName") String tableName);
	
	@Select("<script>"
			+ "select table_schema, table_name, table_comment, create_time, update_time"
			+ " from information_schema.tables"
			+ " where table_schema=(select database())"
			+ "	and lower(table_name) like lower(concat(#{tableNamePrefix}, '%'))"
			+ "</script>")
	public List<DbTable> listDbTableByPrefix(@Param("tableNamePrefix") String tableNamePrefix);
	
	@Select("select table_schema, table_name, table_comment, create_time, update_time"
			+ " from information_schema.tables"
			+ " where table_schema=(select database()) and table_name=#{tableName}")
	public DbTable getDbTable(@Param("tableName") String tableName);

	@Select("select * from information_schema.columns"
			+ " where table_schema=(select database()) and table_name=#{tableName} and column_name != #{ignoreColumn}")
	public List<DbTableColumn> listTableColumn(@Param("tableName") String tableName, @Param("ignoreColumn") String ignoreColumn);
	
	@Select("select * from information_schema.columns"
			+ " where table_schema=(select database())"
			+ " and table_name=#{tableName}"
			+ " and column_name=#{columnName}")
	public DbTableColumn getTabelColumn(@Param("tableName") String tableName, @Param("columnName") String columnName);

	@Delete("DROP TABLE #{tableName}")
    void dropModelDataTable(@Param("tableName") String tableName);
}
