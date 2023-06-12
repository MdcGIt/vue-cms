package com.ruoyi.common.mybatisplus.mapper;

import java.util.List;
import java.util.Map;

import com.ruoyi.common.mybatisplus.db.DBTable;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.ruoyi.common.mybatisplus.db.DBTableColumn;

import jakarta.validation.constraints.NotNull;

@Mapper
public interface MySQLMapper {

	/**
	 * 查找数据库表
	 *
	 * @param tableName
	 * @return
	 */
	@Select("<script>"
			+ "SELECT table_schema, table_name, table_comment, create_time, update_time"
			+ " FROM information_schema.tables"
			+ " WHERE table_schema = (select database())"
			+ "<if test=\"tableName != null and tableName != ''\">"
			+ "	AND table_name = #{tableName}"
			+ "</if>"
			+ "</script>")
	List<DBTable> listTables(@Param("tableName") String tableName);

	@Select("SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = (select database()) AND table_name = #{tableName}")
	List<Map<String, Object>> listTableColumns(@Param("tableName") String tableName);

	@Delete("DROP TABLE #{tableName}")
	void dropTable(@Param("tableName") String tableName);

	@Select("SELECT count(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = (database()) AND TABLE_NAME = #{tableName}")
	Long isTableExists(@Param("tableName") String tableName);

	@Insert("""
			<script>
			CREATE TABLE `${tableName}` (
				<foreach item="column" collection="columns" open="" separator="," close="">
				`${column.name}` ${column.type} <choose><when test=' column.nullable'>NOT NULL</when><otherwise>DEFAULT NULL</otherwise></choose>
				<if test = 'column.autoIncrement'> AUTO_INCREMENT</if>
				</foreach>,
				PRIMARY KEY (
				<foreach item="primaryKey" collection="primaryKeys" open="" separator="," close="">
				`${primaryKey.name}`
				</foreach>)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
			</script>
			""")
	void createBackupTable(@Param("tableName") String tableName,
								  @Param("columns") @NotNull List<DBTableColumn> columns, @Param("primaryKeys") List<DBTableColumn> primaryKeys);

	@Insert("""
			<script>
			INSERT INTO `${tableName}` (
			<foreach item="column" collection="columns" open="" separator="," close="">
			`${column}`
			</foreach>
			) VALUES (
			<foreach item="value" collection="values" open="" separator="," close="">
			#{value}
			</foreach>
			);
			</script>
			""")
	void insertRow(@Param("tableName") String tableName, @Param("columns") @NotNull List<String> columns,
			@Param("values") List<Object> value);

	@Insert("""
			<script>
			INSERT INTO `${sourceTableName}` (
			<foreach item="column" collection="columns" separator=",">
			`${column}`
			</foreach>
			) SELECT 
			<foreach item="column" collection="columns" separator=",">
			`${column}`
			</foreach> 
			FROM `${backupTableName}` WHERE backup_id = #{backupId}
			</script>
			""")
	void recoverBackup(@Param("backupTableName") String backupTableName,
			@Param("sourceTableName") String sourceTableName, @Param("columns") List<String> columns,
			@Param("backupId") Long backupId);

	@Delete("DELETE FROM `${backupTableName}` WHERE backup_id = #{backupId}")
	void deleteBackupById(@Param("backupTableName") String backupTableName, @Param("backupId") Long backupId);

	@Delete("""
			<script>
			DELETE FROM `${backupTableName}` WHERE backup_id in (
			<foreach item="backupId" collection="backupIds" separator=",">
			#{backupId}
			</foreach>
			)
			</script>
			""")
	void deleteBackupByIds(@Param("backupTableName") String backupTableName, @Param("backupIds") List<Long> backupIds);
}
