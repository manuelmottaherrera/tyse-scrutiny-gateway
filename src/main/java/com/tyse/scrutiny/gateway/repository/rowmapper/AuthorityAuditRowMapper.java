package com.tyse.scrutiny.gateway.repository.rowmapper;

import com.tyse.scrutiny.gateway.domain.authorization.AuthorityAudit;
import com.tyse.scrutiny.gateway.domain.enumeration.AuditAction;
import io.r2dbc.spi.Row;
import java.time.Instant;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link AuthorityAudit}, with proper type conversions.
 */
@Service
public class AuthorityAuditRowMapper implements BiFunction<Row, String, AuthorityAudit> {

    private final ColumnConverter converter;

    public AuthorityAuditRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link AuthorityAudit} stored in the database.
     */
    @Override
    public AuthorityAudit apply(Row row, String prefix) {
        AuthorityAudit entity = new AuthorityAudit();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setAuthorityId(converter.fromRow(row, prefix + "_authority_id", Long.class));
        entity.setAction(converter.fromRow(row, prefix + "_action", AuditAction.class));
        entity.setOldValues(converter.fromRow(row, prefix + "_old_values", String.class));
        entity.setNewValues(converter.fromRow(row, prefix + "_new_values", String.class));
        entity.setChangedBy(converter.fromRow(row, prefix + "_changed_by", String.class));
        entity.setChangedDate(converter.fromRow(row, prefix + "_changed_date", Instant.class));
        entity.setIpAddress(converter.fromRow(row, prefix + "_ip_address", String.class));
        entity.setUserAgent(converter.fromRow(row, prefix + "_user_agent", String.class));
        return entity;
    }
}
