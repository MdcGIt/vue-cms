package com.ruoyi.common.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.ruoyi.common.mybatisplus.db.DBTable;
import com.ruoyi.common.mybatisplus.db.DBTableColumn;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库信息获取服务类
 *
 * @author 兮玥
 * @email 190785909@qq.com
 */
@Service
@RequiredArgsConstructor
public class DBService {

    private final DruidDataSource dataSource;

    /**
     * 获取数据库表元数据信息
     *
     * @param tableName
     * @return
     */
    public List<DBTable> listTables(@Nullable String tableName) {
        List<DBTable> tables = new ArrayList<>();
        try {
            Connection connection = dataSource.getConnection().getConnection();
            DatabaseMetaData metaData = connection.getMetaData();

            ResultSet rs = metaData.getTables(connection.getCatalog(), null, tableName, null);
            while (rs.next()) {
                DBTable dbTable = new DBTable();
                dbTable.setCatalog(connection.getCatalog());
                dbTable.setName(rs.getString("TABLE_NAME"));
                dbTable.setType(rs.getString("TABLE_TYPE"));
                dbTable.setComment(rs.getString("REMARKS"));

                this.setTableColumns(dbTable, metaData);
                tables.add(dbTable);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tables;
    }

    private void setTableColumns(DBTable table, DatabaseMetaData metaData) throws SQLException {
        ResultSet rsPrimaryKeys = metaData.getPrimaryKeys(table.getCatalog(), null, table.getName());
        List<String> primaryKeys = new ArrayList<>();
        while(rsPrimaryKeys.next()) {
            primaryKeys.add(rsPrimaryKeys.getString("COLUMN_NAME"));
        }

        ResultSet columns = metaData.getColumns(table.getCatalog(), null, table.getName(), "%");
        while (columns.next()) {
            DBTableColumn column = new DBTableColumn();
            column.setName(columns.getString("COLUMN_NAME"));
            column.setType(columns.getInt("DATA_TYPE"));
            column.setTypeName(columns.getString("TYPE_NAME"));
            column.setSize(columns.getInt("COLUMN_SIZE"));
            column.setDecimalDigits(columns.getInt("DECIMAL_DIGITS"));
            column.setNullable(ResultSetMetaData.columnNullable == columns.getInt("NULLABLE"));
            column.setDefaultValue(columns.getString("COLUMN_DEF"));
            column.setComment(columns.getString("REMARKS"));
            column.setAutoIncrement("YES".equals(columns.getString("IS_AUTOINCREMENT")));
            column.setPrimary(primaryKeys.contains(column.getName()));
            table.getColumns().add(column);
        }
    }
}
