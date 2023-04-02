package com.ruoyi.common.mybatisplus.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.ruoyi.common.mybatisplus.db.TableColumn;

import jakarta.validation.constraints.NotNull;

@Mapper
public interface MySQLMapper {

	/**
	 * 查询指定表名称
	 * 
	 * @param tableSchema
	 * @param tableName
	 * @return
	 */
	@Select("SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=database() AND TABLE_NAME=#{tableName}")
	public List<TableColumn> selectTableColumns(@Param("tableName") String tableName);

	@Select("SELECT count(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=database() AND TABLE_NAME=#{tableName}")
	public Long isTableExists(@Param("tableName") String tableName);

	@Insert("""
			<script>
			CREATE TABLE `${tableName}` (
				<foreach item="column" collection="columns" open="" separator="," close="">
				`${column.columnName}` ${column.columnType} <choose><when test=' column.isNullable == "NO" '>NOT NULL</when><otherwise>DEFAULT NULL</otherwise></choose>
				</foreach>,
				PRIMARY KEY (
				<foreach item="primaryKey" collection="primaryKeys" open="" separator="," close="">
				`${primaryKey.columnName}`
				</foreach>)
			) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
			</script>
			""")
	public void createBackupTable(@Param("tableName") String tableName,
			@Param("columns") @NotNull List<TableColumn> columns, @Param("primaryKeys") List<TableColumn> primaryKeys);

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
	public void insertRow(@Param("tableName") String tableName, @Param("columns") @NotNull List<String> columns,
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
	public void recoverBackup(@Param("backupTableName") String backupTableName,
			@Param("sourceTableName") String sourceTableName, @Param("columns") List<String> columns,
			@Param("backupId") Long backupId);

	@Delete("DELETE FROM `${backupTableName}` WHERE backup_id = #{backupId}")
	public void deleteBackupById(@Param("backupTableName") String backupTableName, @Param("backupId") Long backupId);
}
