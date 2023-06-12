package com.ruoyi.common.mybatisplus.service.impl;

import com.baomidou.mybatisplus.annotation.DbType;
import com.ruoyi.common.mybatisplus.MybatisPlusErrorCode;
import com.ruoyi.common.mybatisplus.db.DBTable;
import com.ruoyi.common.mybatisplus.db.DBTableColumn;
import com.ruoyi.common.mybatisplus.db.IDbType;
import com.ruoyi.common.mybatisplus.service.IDBService;
import com.ruoyi.common.utils.Assert;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <TODO description class purpose>
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class DBServerImpl implements IDBService {

    @Value("${mybatis-plus.dbType:mysql}")
    private String dbTypeStr;

    private final Map<String, IDbType> dbTypeMap;

    @Override
    public IDbType getDbType() {
        IDbType dbType = this.dbTypeMap.get(IDbType.BEAN_PREFIX + dbTypeStr.toLowerCase());
        Assert.notNull(dbType, () -> MybatisPlusErrorCode.UNSUPPORTED_DB_TYPE.exception(dbTypeStr));
        return dbType;
    }

    @Override
    public List<DBTable> listTables(String tableName) {
        return this.getDbType().listTables(tableName);
    }

    @Override
    public List<DBTableColumn> listTableColumns(String tableName) {
        return this.getDbType().listTableColumns(tableName);
    }

    @Override
    public void dropTable(String tableName) {
        this.getDbType().dropTable(tableName);
    }
}
