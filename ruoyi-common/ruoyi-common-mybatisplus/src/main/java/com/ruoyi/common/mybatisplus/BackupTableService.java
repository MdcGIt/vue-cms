package com.ruoyi.common.mybatisplus;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.mybatisplus.annotation.BackupTable;
import com.ruoyi.common.mybatisplus.db.IDbType;
import com.ruoyi.common.utils.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackupTableService implements CommandLineRunner {

	static final String RESOURCE_PATTERN = "/**/*.class";

	@Value("${mybatis-plus.dbType:}")
	private String dbTypeStr;

	@Value("${mybatis-plus.typeAliasesPackage:com.ruoyi.**.domain}")
	private String typeAliasesPackage;

	private final List<IDbType> dbTypes;

	/**
	 * 创建备份表
	 */
	public void run(String... args) throws IOException, ClassNotFoundException {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
				+ ClassUtils.convertClassNameToResourcePath(this.typeAliasesPackage) + RESOURCE_PATTERN;
		Resource[] resources = resourcePatternResolver.getResources(pattern);
		MetadataReaderFactory readerfactory = new CachingMetadataReaderFactory(resourcePatternResolver);
		for (Resource resource : resources) {
			MetadataReader reader = readerfactory.getMetadataReader(resource);
			String classname = reader.getClassMetadata().getClassName();
			Class<?> clazz = Class.forName(classname);
			BackupTable anno = clazz.getAnnotation(BackupTable.class);
			if (anno != null) {
				TableName annoTableName = clazz.getAnnotation(TableName.class);
				if (annoTableName != null) {
					String tableName = annoTableName.value();
					// 复制此table字段，创建备份表
					this.createBackupTable(tableName);
				}
			}
		}
	}

	/**
	 * 备份数据
	 * 
	 * @param <T>
	 * @param entity
	 * @param backupOperator
	 * @param backupRemark
	 */
	public <T> void backup(T entity, Long backupId, String backupOperator, String backupRemark) {
		DbType dbType = StringUtils.isEmpty(dbTypeStr) ? DbType.MYSQL : DbType.getDbType(dbTypeStr);
		this.dbTypes.stream().filter(dt -> dbType.getDb().equals(dt.getType())).findFirst().ifPresentOrElse(
				dt -> dt.backup(entity, backupId, backupOperator, backupRemark),
				() -> log.warn("IDbType not found: " + dbTypeStr));
	}

	public void recover(Long backupId, Class<?> entityClazz) {
		DbType dbType = StringUtils.isEmpty(dbTypeStr) ? DbType.MYSQL : DbType.getDbType(dbTypeStr);
		this.dbTypes.stream().filter(dt -> dbType.getDb().equals(dt.getType())).findFirst().ifPresentOrElse(
				dt -> dt.recover(backupId, entityClazz),
				() -> log.warn("IDbType not found: " + dbTypeStr));
	}

	/**
	 * 创建备份表
	 */
	private void createBackupTable(String sourceTable) {
		DbType dbType = StringUtils.isEmpty(dbTypeStr) ? DbType.MYSQL : DbType.getDbType(dbTypeStr);
		this.dbTypes.stream().filter(dt -> dbType.getDb().equals(dt.getType())).findFirst().ifPresentOrElse(
				dt -> dt.createBackupTable(sourceTable), () -> log.warn("IDbType not found: " + dbTypeStr));
	}
}
