package com.tyse.scrutiny.gateway.repository.authorization;

import com.tyse.scrutiny.gateway.domain.authorization.AuthorityAudit;
import com.tyse.scrutiny.gateway.domain.enumeration.AuditAction;
import java.time.Instant;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Spring Data R2DBC repository for the AuthorityAudit entity.
 * This repository is append-only (no updates or deletes) for audit trail integrity.
 */
@Repository
public interface AuthorityAuditRepository extends R2dbcRepository<AuthorityAudit, Long> {
    /**
     * Find all audit entries for a specific authority.
     * Ordered by most recent first.
     */
    Flux<AuthorityAudit> findByAuthorityIdOrderByChangedDateDesc(Long authorityId);

    /**
     * Find audit entries by who made the change.
     */
    Flux<AuthorityAudit> findByChangedByOrderByChangedDateDesc(String changedBy);

    /**
     * Find audit entries by action type.
     * Example: findByAction(AuditAction.DELETED) for all deletions
     */
    Flux<AuthorityAudit> findByActionOrderByChangedDateDesc(AuditAction action);

    /**
     * Find audit entries for an authority by action type.
     */
    Flux<AuthorityAudit> findByAuthorityIdAndActionOrderByChangedDateDesc(Long authorityId, AuditAction action);

    /**
     * Find audit entries within a date range.
     */
    Flux<AuthorityAudit> findByChangedDateBetweenOrderByChangedDateDesc(Instant start, Instant end);

    /**
     * Find recent audit entries (paginated).
     * Use for audit log dashboards.
     */
    Flux<AuthorityAudit> findAllBy(Pageable pageable);

    /**
     * Find audit entries for a specific authority with pagination.
     */
    Flux<AuthorityAudit> findByAuthorityId(Long authorityId, Pageable pageable);
}
