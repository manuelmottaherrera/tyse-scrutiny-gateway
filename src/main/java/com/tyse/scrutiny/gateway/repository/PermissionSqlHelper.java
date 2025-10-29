package com.tyse.scrutiny.gateway.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class PermissionSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("name", table, columnPrefix + "_name"));
        columns.add(Column.aliased("resource", table, columnPrefix + "_resource"));
        columns.add(Column.aliased("action", table, columnPrefix + "_action"));
        columns.add(Column.aliased("description", table, columnPrefix + "_description"));
        columns.add(Column.aliased("is_active", table, columnPrefix + "_is_active"));
        columns.add(Column.aliased("created_by", table, columnPrefix + "_created_by"));
        columns.add(Column.aliased("created_date", table, columnPrefix + "_created_date"));
        columns.add(Column.aliased("last_modified_by", table, columnPrefix + "_last_modified_by"));
        columns.add(Column.aliased("last_modified_date", table, columnPrefix + "_last_modified_date"));
        return columns;
    }
}
