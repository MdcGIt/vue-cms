package com.ruoyi.common.mybatisplus.config;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.ruoyi.common.utils.JacksonUtils;
import com.ruoyi.common.utils.StringUtils;

@Configuration
@MapperScan("com.ruoyi.**.mapper")
public class MybatisPlusConfiguration {
	
	/**
	 * 常见驱动对应数据库类型
	 */
	private static Map<String, DbType> DriverClassNameMap = new HashMap<>(1);

	static {
		DriverClassNameMap.put("com.mysql.cj.jdbc.Driver", DbType.MYSQL);
		DriverClassNameMap.put("org.mariadb.jdbc.Driver", DbType.MARIADB);
		DriverClassNameMap.put("com.ibm.db2.jcc.DB2Driver", DbType.DB2);
		DriverClassNameMap.put("org.hsqldb.jdbcDriver", DbType.HSQL);
		DriverClassNameMap.put("org.postgresql.Driver", DbType.POSTGRE_SQL);
		DriverClassNameMap.put("org.sqlite.JDBC", DbType.SQLITE);
		DriverClassNameMap.put("oracle.jdbc.driver.OracleDriver", DbType.ORACLE);
		DriverClassNameMap.put("com.microsoft.sqlserver.jdbc.SQLServerDriver", DbType.SQL_SERVER);
		// jackson config
		JacksonTypeHandler.setObjectMapper(JacksonUtils.getObjectMapper());
	}

	/*
	 * 数据库类型
	 */
	@Value("${mybatis-plus.dbType:}")
	private String dbTypeStr;

	/*
	 * 数据驱动
	 */
	@Value("${spring.datasource.driver-class-name:}")
	private String driverClassName;

	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		if (StringUtils.isEmpty(dbTypeStr)) {
			dbTypeStr = DbType.MYSQL.getDb();
		}
		DbType dbType = DbType.getDbType(dbTypeStr);
		if (dbType == DbType.OTHER && DriverClassNameMap.containsKey(this.driverClassName)) {
			dbType = DriverClassNameMap.get(this.driverClassName);
		}
		if (dbType == null || dbType == DbType.OTHER) {
			throw new RuntimeException(MessageFormat.format("mybatis-plus interceptor error: dbtype invalid for '{0}'", this.dbTypeStr));
		}
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType)); // 分页支持
		interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor()); // 防止全表更新与删除
		// interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor()); //
		return interceptor;
	}

	@Bean
	public ConfigurationCustomizer customizer() {
		return new ConfigurationCustomizer() {

			@Override
			public void customize(MybatisConfiguration configuration) {
				configuration.setDefaultEnumTypeHandler(MybatisEnumTypeHandler.class);
			}
		};
	}
}