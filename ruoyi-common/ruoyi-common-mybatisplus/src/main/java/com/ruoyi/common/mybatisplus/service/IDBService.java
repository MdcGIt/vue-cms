package com.ruoyi.common.mybatisplus.service;

import com.ruoyi.common.mybatisplus.db.DBTable;
import com.ruoyi.common.mybatisplus.db.DBTableColumn;
import com.ruoyi.common.mybatisplus.db.IDbType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import java.util.List;

/**
 * 数据库服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
public interface IDBService {

    /**
     * 获取数据库类型定义实例
     *
     * @return
     */
    IDbType getDbType();

    /**
     * 查询指定表名的数据库表集合
     *
     * @param tableName
     * @return
     */
    List<DBTable> listTables(@Nullable String tableName);

    /**
     * 查询指定表
     *
     * @param tableName
     * @return
     */
    List<DBTableColumn> listTableColumns(@NotNull String tableName);

    /**
     * 删除指定表
     *
     * @param tableName
     */
    void dropTable(@NotNull String tableName);
}
