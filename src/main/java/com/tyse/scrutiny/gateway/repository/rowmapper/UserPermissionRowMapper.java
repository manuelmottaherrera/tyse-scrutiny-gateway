package com.tyse.scrutiny.gateway.repository.rowmapper;

import com.tyse.scrutiny.gateway.domain.authorization.UserPermission;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link UserPermission}, with proper type conversions.
 */
@Service
public class UserPermissionRowMapper implements BiFunction<Row, String, UserPermission> {

    private final ColumnConverter converter;

    public UserPermissionRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link UserPermission} stored in the database.
     */
    @Override
    public UserPermission apply(Row row, String prefix) {
        UserPermission entity = new UserPermission();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setUserId(converter.fromRow(row, prefix + "_user_id", Long.class));
        entity.setPermissionId(converter.fromRow(row, prefix + "_permission_id", Long.class));
        entity.setIsActive(Boolean.TRUE.equals(converter.fromRow(row, prefix + "_is_active", Boolean.class)));
        entity.setExpiresAt(converter.fromRow(row, prefix + "_expires_at", Instant.class));
        entity.setGrantedBy(converter.fromRow(row, prefix + "_granted_by", String.class));
        entity.setGrantedDate(converter.fromRow(row, prefix + "_granted_date", Instant.class));
        entity.setReason(converter.fromRow(row, prefix + "_reason", String.class));
        entity.setRevokedBy(converter.fromRow(row, prefix + "_revoked_by", String.class));
        entity.setRevokedDate(converter.fromRow(row, prefix + "_revoked_date", Instant.class));
        entity.setRevokedReason(converter.fromRow(row, prefix + "_revoked_reason", String.class));
        return entity;
    }
}
