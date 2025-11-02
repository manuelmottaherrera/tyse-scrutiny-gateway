package com.tyse.scrutiny.gateway.service.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyse.scrutiny.gateway.service.dto.authorization.AuditSearchCriteria;
import com.tyse.scrutiny.gateway.service.dto.authorization.AuthorityAuditDTO;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Service for exporting authority audit logs to different formats.
 */
@Service
@Transactional(readOnly = true)
public class AuditExportService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final AuthorityAuditService auditService;
    private final ObjectMapper objectMapper;

    public AuditExportService(AuthorityAuditService auditService, ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    /**
     * Export audit logs to CSV format.
     *
     * @param criteria search criteria for filtering
     * @return Mono containing CSV as byte array
     */
    public Mono<byte[]> exportToCsv(AuditSearchCriteria criteria) {
        LOG.debug("Exporting audits to CSV with criteria: {}", criteria);

        // Use a large page size for export (max 10000 records to prevent memory issues)
        Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "changedDate"));

        return auditService
            .searchAudits(criteria, pageable)
            .collectList()
            .map(this::convertToCsv)
            .map(csv -> csv.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Export audit logs to JSON format.
     *
     * @param criteria search criteria for filtering
     * @return Mono containing JSON as byte array
     */
    public Mono<byte[]> exportToJson(AuditSearchCriteria criteria) {
        LOG.debug("Exporting audits to JSON with criteria: {}", criteria);

        // Use a large page size for export (max 10000 records to prevent memory issues)
        Pageable pageable = PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "changedDate"));

        return auditService
            .searchAudits(criteria, pageable)
            .collectList()
            .map(this::convertToJson)
            .map(json -> json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Convert list of audit DTOs to CSV format.
     *
     * @param audits list of audit DTOs
     * @return CSV string
     */
    private String convertToCsv(List<AuthorityAuditDTO> audits) {
        StringBuilder csv = new StringBuilder();

        // CSV Header
        csv
            .append("ID,")
            .append("Authority ID,")
            .append("Authority Code,")
            .append("Action Type,")
            .append("Changed By,")
            .append("Changed Date,")
            .append("IP Address,")
            .append("Old Values,")
            .append("New Values")
            .append("\n");

        // CSV Rows
        for (AuthorityAuditDTO audit : audits) {
            csv
                .append(audit.getId() != null ? audit.getId() : "")
                .append(",")
                .append(audit.getAuthorityId() != null ? audit.getAuthorityId() : "")
                .append(",")
                .append(escapeCsvValue(audit.getAuthorityCode()))
                .append(",")
                .append(audit.getAction() != null ? audit.getAction().name() : "")
                .append(",")
                .append(escapeCsvValue(audit.getChangedBy()))
                .append(",")
                .append(audit.getChangedDate() != null ? DATE_FORMATTER.format(audit.getChangedDate()) : "")
                .append(",")
                .append(escapeCsvValue(audit.getIpAddress()))
                .append(",")
                .append(escapeCsvValue(audit.getOldValues()))
                .append(",")
                .append(escapeCsvValue(audit.getNewValues()))
                .append("\n");
        }

        return csv.toString();
    }

    /**
     * Convert list of audit DTOs to JSON format.
     *
     * @param audits list of audit DTOs
     * @return JSON string
     */
    private String convertToJson(List<AuthorityAuditDTO> audits) {
        try {
            return objectMapper.writeValueAsString(audits);
        } catch (JsonProcessingException e) {
            LOG.error("Error converting audits to JSON", e);
            return "[]";
        }
    }

    /**
     * Escape CSV value to handle special characters (quotes, commas, newlines).
     *
     * @param value the value to escape
     * @return escaped value
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }

        // If value contains comma, quote, or newline, wrap in quotes and escape internal quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
