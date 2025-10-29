package com.tyse.scrutiny.gateway.repository.rowmapper;

import com.tyse.scrutiny.gateway.domain.authorization.UserAuthority;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link UserAuthority}, with proper type conversions.
 */
@Service
public class UserAuthorityRowMapper implements BiFunction<Row, String, UserAuthority> {

    private final ColumnConverter converter;

    public UserAuthorityRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link UserAuthority} stored in the database.
     */
    @Override
    public UserAuthority apply(Row row, String prefix) {
        UserAuthority entity = new UserAuthority();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setUserId(converter.fromRow(row, prefix + "_user_id", Long.class));
        entity.setAuthorityId(converter.fromRow(row, prefix + "_authority_id", Long.class));
        entity.setIsActive(Boolean.TRUE.equals(converter.fromRow(row, prefix + "_is_active", Boolean.class)));
        entity.setExpiresAt(converter.fromRow(row, prefix + "_expires_at", Instant.class));
        entity.setAssignedBy(converter.fromRow(row, prefix + "_assigned_by", String.class));
        entity.setAssignedDate(converter.fromRow(row, prefix + "_assigned_date", Instant.class));
        entity.setRevokedBy(converter.fromRow(row, prefix + "_revoked_by", String.class));
        entity.setRevokedDate(converter.fromRow(row, prefix + "_revoked_date", Instant.class));
        entity.setRevokedReason(converter.fromRow(row, prefix + "_revoked_reason", String.class));
        return entity;
    }
}
