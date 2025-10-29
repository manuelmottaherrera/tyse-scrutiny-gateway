package com.tyse.scrutiny.gateway.repository.rowmapper;

import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Permission}, with proper type conversions.
 */
@Service
public class PermissionRowMapper implements BiFunction<Row, String, Permission> {

    private final ColumnConverter converter;

    public PermissionRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Permission} stored in the database.
     */
    @Override
    public Permission apply(Row row, String prefix) {
        Permission entity = new Permission();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setResource(converter.fromRow(row, prefix + "_resource", String.class));
        entity.setAction(converter.fromRow(row, prefix + "_action", String.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        entity.setIsActive(Boolean.TRUE.equals(converter.fromRow(row, prefix + "_is_active", Boolean.class)));
        entity.setCreatedBy(converter.fromRow(row, prefix + "_created_by", String.class));
        entity.setCreatedDate(converter.fromRow(row, prefix + "_created_date", Instant.class));
        entity.setLastModifiedBy(converter.fromRow(row, prefix + "_last_modified_by", String.class));
        entity.setLastModifiedDate(converter.fromRow(row, prefix + "_last_modified_date", Instant.class));
        return entity;
    }
}
