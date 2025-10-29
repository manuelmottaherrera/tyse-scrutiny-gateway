package com.tyse.scrutiny.gateway.repository;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Table;

public class AuthorityAuditSqlHelper {

    public static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("authority_id", table, columnPrefix + "_authority_id"));
        columns.add(Column.aliased("action", table, columnPrefix + "_action"));
        columns.add(Column.aliased("old_values", table, columnPrefix + "_old_values"));
        columns.add(Column.aliased("new_values", table, columnPrefix + "_new_values"));
        columns.add(Column.aliased("changed_by", table, columnPrefix + "_changed_by"));
        columns.add(Column.aliased("changed_date", table, columnPrefix + "_changed_date"));
        columns.add(Column.aliased("ip_address", table, columnPrefix + "_ip_address"));
        columns.add(Column.aliased("user_agent", table, columnPrefix + "_user_agent"));
        return columns;
    }
}
