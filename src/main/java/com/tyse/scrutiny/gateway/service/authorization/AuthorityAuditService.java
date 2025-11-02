package com.tyse.scrutiny.gateway.service.authorization;

import com.tyse.scrutiny.gateway.domain.authorization.AuthorityAudit;
import com.tyse.scrutiny.gateway.domain.enumeration.AuditAction;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.authorization.AuthorityAuditRepository;
import com.tyse.scrutiny.gateway.service.dto.authorization.ActivityPeriodDTO;
import com.tyse.scrutiny.gateway.service.dto.authorization.AuditMetricsSummaryDTO;
import com.tyse.scrutiny.gateway.service.dto.authorization.AuditSearchCriteria;
import com.tyse.scrutiny.gateway.service.dto.authorization.AuthorityAuditDTO;
import com.tyse.scrutiny.gateway.service.dto.authorization.UserActivityDTO;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for querying authority audit logs.
 * This is a read-only service - audit logs are created by AuthorityService.
 */
@Service
@Transactional(readOnly = true)
public class AuthorityAuditService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorityAuditService.class);

    private final AuthorityAuditRepository auditRepository;
    private final AuthorityRepository authorityRepository;
    private final DatabaseClient databaseClient;

    public AuthorityAuditService(
        AuthorityAuditRepository auditRepository,
        AuthorityRepository authorityRepository,
        DatabaseClient databaseClient
    ) {
        this.auditRepository = auditRepository;
        this.authorityRepository = authorityRepository;
        this.databaseClient = databaseClient;
    }

    /**
     * Get all audit logs with pagination.
     *
     * @param pageable pagination information
     * @return flux of audit DTOs
     */
    public Flux<AuthorityAuditDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all AuthorityAudits with pagination: {}", pageable);
        return auditRepository.findAllBy(pageable).flatMap(this::mapToDTO);
    }

    /**
     * Get a single audit log by ID.
     *
     * @param id the id of the audit log
     * @return mono of audit DTO
     */
    public Mono<AuthorityAuditDTO> findOne(Long id) {
        LOG.debug("Request to get AuthorityAudit : {}", id);
        return auditRepository.findById(id).flatMap(this::mapToDTO);
    }

    /**
     * Get all audit logs for a specific authority.
     *
     * @param authorityId the authority id
     * @return flux of audit DTOs
     */
    public Flux<AuthorityAuditDTO> findByAuthorityId(Long authorityId) {
        LOG.debug("Request to get audits for authority: {}", authorityId);
        return auditRepository.findByAuthorityIdOrderByChangedDateDesc(authorityId).flatMap(this::mapToDTO);
    }

    /**
     * Search audit logs with advanced criteria.
     *
     * @param criteria the search criteria
     * @param pageable pagination information
     * @return flux of audit DTOs matching the criteria
     */
    public Flux<AuthorityAuditDTO> searchAudits(AuditSearchCriteria criteria, Pageable pageable) {
        LOG.debug("Request to search audits with criteria: {}", criteria);

        // Build dynamic query based on criteria
        StringBuilder sql = new StringBuilder("SELECT * FROM scr_authority_audit WHERE 1=1");

        if (criteria.getAuthorityId() != null) {
            sql.append(" AND authority_id = :authorityId");
        }
        if (criteria.getChangedBy() != null && !criteria.getChangedBy().isEmpty()) {
            sql.append(" AND changed_by = :changedBy");
        }
        if (criteria.getAction() != null) {
            sql.append(" AND action = :action");
        }
        if (criteria.getFromDate() != null) {
            sql.append(" AND changed_date >= :fromDate");
        }
        if (criteria.getToDate() != null) {
            sql.append(" AND changed_date <= :toDate");
        }

        sql.append(" ORDER BY changed_date DESC");
        sql.append(" LIMIT :limit OFFSET :offset");

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql.toString());

        // Bind parameters
        if (criteria.getAuthorityId() != null) {
            executeSpec = executeSpec.bind("authorityId", criteria.getAuthorityId());
        }
        if (criteria.getChangedBy() != null && !criteria.getChangedBy().isEmpty()) {
            executeSpec = executeSpec.bind("changedBy", criteria.getChangedBy());
        }
        if (criteria.getAction() != null) {
            executeSpec = executeSpec.bind("action", criteria.getAction().name());
        }
        if (criteria.getFromDate() != null) {
            executeSpec = executeSpec.bind("fromDate", criteria.getFromDate());
        }
        if (criteria.getToDate() != null) {
            executeSpec = executeSpec.bind("toDate", criteria.getToDate());
        }

        executeSpec = executeSpec.bind("limit", pageable.getPageSize()).bind("offset", pageable.getOffset());

        return executeSpec
            .map((row, metadata) -> {
                AuthorityAudit audit = new AuthorityAudit();
                audit.setId(row.get("id", Long.class));
                audit.setAuthorityId(row.get("authority_id", Long.class));
                audit.setAction(AuditAction.valueOf(row.get("action", String.class)));
                audit.setOldValues(row.get("old_values", String.class));
                audit.setNewValues(row.get("new_values", String.class));
                audit.setChangedBy(row.get("changed_by", String.class));
                audit.setChangedDate(row.get("changed_date", Instant.class));
                audit.setIpAddress(row.get("ip_address", String.class));
                audit.setUserAgent(row.get("user_agent", String.class));
                return audit;
            })
            .all()
            .flatMap(this::mapToDTO);
    }

    /**
     * Get recent audit logs (last N entries).
     *
     * @param limit the maximum number of entries to return
     * @return flux of recent audit DTOs
     */
    public Flux<AuthorityAuditDTO> findRecent(int limit) {
        LOG.debug("Request to get {} recent audits", limit);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "changedDate"));
        return auditRepository.findAllBy(pageable).flatMap(this::mapToDTO);
    }

    /**
     * Count audit logs by action type.
     *
     * @param action the action type
     * @return mono with the count
     */
    public Mono<Long> countByActionType(AuditAction action) {
        LOG.debug("Request to count audits by action: {}", action);
        return auditRepository.findByActionOrderByChangedDateDesc(action).count();
    }

    /**
     * Count audit logs by user who made the change.
     *
     * @param username the username
     * @return mono with the count
     */
    public Mono<Long> countByChangedBy(String username) {
        LOG.debug("Request to count audits by user: {}", username);
        return auditRepository.findByChangedByOrderByChangedDateDesc(username).count();
    }

    /**
     * Count total audit logs.
     *
     * @return mono with the total count
     */
    public Mono<Long> countAll() {
        LOG.debug("Request to count all audits");
        return auditRepository.count();
    }

    /**
     * Get audits for the last N hours.
     *
     * @param hours number of hours to look back
     * @return flux of audit DTOs
     */
    public Flux<AuthorityAuditDTO> findByLastHours(int hours) {
        LOG.debug("Request to get audits from last {} hours", hours);
        Instant fromDate = Instant.now().minusSeconds(hours * 3600L);
        Instant toDate = Instant.now();
        return auditRepository.findByChangedDateBetweenOrderByChangedDateDesc(fromDate, toDate).flatMap(this::mapToDTO);
    }

    /**
     * Get comprehensive metrics summary for audit logs.
     *
     * @return mono of audit metrics summary
     */
    public Mono<AuditMetricsSummaryDTO> getMetricsSummary() {
        LOG.debug("Request to get audit metrics summary");

        return Mono.zip(getTotalCount(), getAuditsByAction(), getTopUsers(), getActivityByPeriod()).map(tuple ->
            new AuditMetricsSummaryDTO(tuple.getT1(), tuple.getT2(), tuple.getT3(), tuple.getT4())
        );
    }

    /**
     * Get metrics for a specific authority.
     *
     * @param authorityId the authority id
     * @return mono of audit metrics summary for the authority
     */
    public Mono<Map<String, Object>> getMetricsByAuthority(Long authorityId) {
        LOG.debug("Request to get metrics for authority: {}", authorityId);

        return Mono.zip(
            auditRepository.findByAuthorityIdOrderByChangedDateDesc(authorityId).count(),
            getActionCountsForAuthority(authorityId),
            getLastChangeForAuthority(authorityId)
        ).map(tuple -> {
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("totalChanges", tuple.getT1());
            metrics.put("changesByAction", tuple.getT2());
            metrics.put("lastChange", tuple.getT3());
            return metrics;
        });
    }

    /**
     * Get total count of audit logs.
     */
    private Mono<Long> getTotalCount() {
        return auditRepository.count();
    }

    /**
     * Get count of audits by action type.
     */
    private Mono<Map<String, Long>> getAuditsByAction() {
        return Flux.fromArray(AuditAction.values())
            .flatMap(action -> countByActionType(action).map(count -> Map.entry(action.name(), count)))
            .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    /**
     * Get top users by audit activity (top 10).
     */
    private Mono<List<UserActivityDTO>> getTopUsers() {
        String sql =
            "SELECT changed_by, COUNT(*) as count " +
            "FROM scr_authority_audit " +
            "GROUP BY changed_by " +
            "ORDER BY count DESC " +
            "LIMIT 10";

        return databaseClient
            .sql(sql)
            .map((row, metadata) -> new UserActivityDTO(row.get("changed_by", String.class), row.get("count", Long.class)))
            .all()
            .collectList();
    }

    /**
     * Get activity by time periods (24h, 7d, 30d).
     */
    private Mono<ActivityPeriodDTO> getActivityByPeriod() {
        Instant now = Instant.now();
        Instant last24h = now.minus(24, ChronoUnit.HOURS);
        Instant last7d = now.minus(7, ChronoUnit.DAYS);
        Instant last30d = now.minus(30, ChronoUnit.DAYS);

        return Mono.zip(
            auditRepository.findByChangedDateBetweenOrderByChangedDateDesc(last24h, now).count(),
            auditRepository.findByChangedDateBetweenOrderByChangedDateDesc(last7d, now).count(),
            auditRepository.findByChangedDateBetweenOrderByChangedDateDesc(last30d, now).count()
        ).map(tuple -> new ActivityPeriodDTO(tuple.getT1(), tuple.getT2(), tuple.getT3()));
    }

    /**
     * Get action counts for a specific authority.
     */
    private Mono<Map<String, Long>> getActionCountsForAuthority(Long authorityId) {
        return Flux.fromArray(AuditAction.values())
            .flatMap(action ->
                auditRepository
                    .findByAuthorityIdAndActionOrderByChangedDateDesc(authorityId, action)
                    .count()
                    .map(count -> Map.entry(action.name(), count))
            )
            .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    /**
     * Get the last change for a specific authority.
     */
    private Mono<AuthorityAuditDTO> getLastChangeForAuthority(Long authorityId) {
        return auditRepository
            .findByAuthorityIdOrderByChangedDateDesc(authorityId)
            .next()
            .flatMap(this::mapToDTO)
            .defaultIfEmpty(new AuthorityAuditDTO());
    }

    /**
     * Map AuthorityAudit entity to DTO and enrich with authority code.
     *
     * @param audit the audit entity
     * @return mono of audit DTO
     */
    private Mono<AuthorityAuditDTO> mapToDTO(AuthorityAudit audit) {
        AuthorityAuditDTO dto = new AuthorityAuditDTO(audit);

        // Enrich with authority code if available
        return authorityRepository
            .findById(audit.getAuthorityId())
            .map(authority -> {
                dto.setAuthorityCode(authority.getCode());
                return dto;
            })
            .defaultIfEmpty(dto);
    }
}
