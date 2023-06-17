package com.ruoyi.common.mybatisplus.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.ruoyi.common.mybatisplus.db.DBTable;
import com.ruoyi.common.mybatisplus.db.FieldValue;
import org.apache.ibatis.annotations.*;

import com.ruoyi.common.mybatisplus.db.DBTableColumn;

import jakarta.validation.constraints.NotNull;

@Mapper
public interface MySQLMapper {

	/**
	 * 查找当前数据库中数据表
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

	/**
	 * 查找指定数据表所有字段
	 *
	 * @param tableName
	 * @return
	 */
	@Select("SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = (select database()) AND table_name = #{tableName}")
	List<Map<String, Object>> listTableColumns(@Param("tableName") String tableName);

	/**
	 * 删除数据表
	 *
	 * @param tableName
	 */
	@Delete("DROP TABLE #{tableName}")
	void dropTable(@Param("tableName") String tableName);

	/**
	 * 指定表是否存在
	 *
	 * @param tableName
	 * @return
	 */
	@Select("SELECT count(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = (database()) AND TABLE_NAME = #{tableName}")
	Long isTableExists(@Param("tableName") String tableName);

	/**
	 * 根据条件查找指定表的一条数据
	 *
	 * @param tableName
	 * @param whereSql
	 * @return
	 */
	@Select("SELECT * FROM `${tableName}` ${whereSql} limit 1")
	Map<String, Object> selectOne(@Param("tableName") String tableName, @Param("whereSql") String whereSql);

	/**
	 * 根据条件查找指定表数据列表
	 *
	 * @param tableName
	 * @param whereSql
	 * @return
	 */
	@Select("SELECT * FROM `${tableName}` ${whereSql} limit 1")
	List<Map<String, Object>> selectList(@Param("tableName") String tableName, @Param("whereSql") String whereSql);

	/**
	 * 向指定表插入一条数据
	 *
	 * @param tableName
	 * @param columns
	 * @param values
	 */
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
	void insertRow(@Param("tableName") String tableName, @Param("columns") List<String> columns,
			@Param("values") List<Object> values);

	/**
	 * 更新指定表中指定条件的数据
	 *
	 * @param tableName
	 * @param fields
	 * @param whereSql
	 * @return
	 */
	@Update("""
			<script>
			UPDATE `${tableName}` SET 
			<foreach item="field" collection="fields" open="" separator="," close="">
			 `${field.name}` = #{field.value} 
			</foreach>
			 ${whereSql};
			</script>
			""")
	public boolean updateRow(@Param("tableName") String tableName,
										 @Param("updateFields") List<FieldValue> fields, @Param("whereSql") String whereSql);

	/**
	 * 删除指定表指定条件的数据
	 *
	 * @param tableName
	 * @param whereSql
	 */
	@Delete("DELETE FROM `${tableName}` ${whereSql};")
	public void deleteRows(@Param("tableName") String tableName, @Param("whereSql") String whereSql);


	/**
	 * 创建备份数据表
	 *
	 * @param tableName
	 * @param columns
	 * @param primaryKeys
	 */
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
						   @Param("columns") @NotNull List<DBTableColumn> columns,
						   @Param("primaryKeys") List<DBTableColumn> primaryKeys);

	/**
	 * 插入备份数据
	 *
	 * @param backupTableName
	 * @param sourceTableName
	 * @param columns
	 * @param backupId
	 */
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

	/**
	 * 删除备份数据
	 *
	 * @param backupTableName
	 * @param backupId
	 */
	@Delete("DELETE FROM `${backupTableName}` WHERE backup_id = #{backupId}")
	void deleteBackupById(@Param("backupTableName") String backupTableName, @Param("backupId") Long backupId);

	/**
	 * 删除备份数据
	 *
	 * @param backupTableName
	 * @param backupIds
	 */
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
