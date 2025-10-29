package com.tyse.scrutiny.gateway.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class UserPermissionSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("user_id", table, columnPrefix + "_user_id"));
        columns.add(Column.aliased("permission_id", table, columnPrefix + "_permission_id"));
        columns.add(Column.aliased("is_active", table, columnPrefix + "_is_active"));
        columns.add(Column.aliased("expires_at", table, columnPrefix + "_expires_at"));
        columns.add(Column.aliased("granted_by", table, columnPrefix + "_granted_by"));
        columns.add(Column.aliased("granted_date", table, columnPrefix + "_granted_date"));
        columns.add(Column.aliased("reason", table, columnPrefix + "_reason"));
        columns.add(Column.aliased("revoked_by", table, columnPrefix + "_revoked_by"));
        columns.add(Column.aliased("revoked_date", table, columnPrefix + "_revoked_date"));
        columns.add(Column.aliased("revoked_reason", table, columnPrefix + "_revoked_reason"));
        return columns;
    }
}
