package com.ruoyi.common.mybatisplus.mapper;

import com.ruoyi.common.mybatisplus.db.DBTableColumn;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MySQLMapper {

	/**
	 * 删除数据表
	 *
	 * @param tableName
	 */
	@Delete("DROP TABLE #{tableName}")
	void dropTable(@Param("tableName") String tableName);

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
