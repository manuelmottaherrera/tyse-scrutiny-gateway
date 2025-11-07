package com.tyse.scrutiny.gateway.service.authorization;

import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.authorization.AuthorityAudit;
import com.tyse.scrutiny.gateway.domain.authorization.UserAuthority;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.UserRepository;
import com.tyse.scrutiny.gateway.repository.authorization.*;
import com.tyse.scrutiny.gateway.service.dto.dashboard.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service class for dashboard metrics and statistics.
 */
@Service
@Transactional(readOnly = true)
public class AuthorizationDashboardService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationDashboardService.class);

    private final AuthorityRepository authorityRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final AuthorityAuditRepository auditRepository;
    private final AuthorityPermissionRepository authorityPermissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final DatabaseClient databaseClient;

    public AuthorizationDashboardService(
        AuthorityRepository authorityRepository,
        PermissionRepository permissionRepository,
        UserRepository userRepository,
        UserAuthorityRepository userAuthorityRepository,
        AuthorityAuditRepository auditRepository,
        AuthorityPermissionRepository authorityPermissionRepository,
        UserPermissionRepository userPermissionRepository,
        DatabaseClient databaseClient
    ) {
        this.authorityRepository = authorityRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.userAuthorityRepository = userAuthorityRepository;
        this.auditRepository = auditRepository;
        this.authorityPermissionRepository = authorityPermissionRepository;
        this.userPermissionRepository = userPermissionRepository;
        this.databaseClient = databaseClient;
    }

    /**
     * Get all dashboard metrics in a single call.
     *
     * @return dashboard metrics DTO
     */
    public Mono<DashboardMetricsDTO> getDashboardMetrics() {
        LOG.debug("Request to get dashboard metrics");

        return Mono.zip(
            getTotalAuthorities(),
            getTotalPermissions(),
            getActiveUsersCount(),
            getExpiredRolesCount(),
            getRecentActivity(10).collectList(),
            getExpiringRoles(7).collectList(),
            getTopAuthorities(10).collectList(),
            getPermissionUsage(10).collectList()
        )
            .map(tuple -> {
                DashboardMetricsDTO dto = new DashboardMetricsDTO();
                dto.setTotalAuthorities(tuple.getT1());
                dto.setTotalPermissions(tuple.getT2());
                dto.setActiveUsers(tuple.getT3());
                dto.setExpiredRoles(tuple.getT4());
                dto.setRecentActivity(tuple.getT5());
                dto.setExpiringRoles(tuple.getT6());
                dto.setTopAuthorities(tuple.getT7());
                dto.setPermissionUsage(tuple.getT8());
                return dto;
            })
            .doOnNext(metrics -> LOG.debug("Dashboard metrics retrieved: {}", metrics));
    }

    /**
     * Get total number of authorities.
     */
    private Mono<Long> getTotalAuthorities() {
        return authorityRepository.count();
    }

    /**
     * Get total number of permissions.
     */
    private Mono<Long> getTotalPermissions() {
        return permissionRepository.count();
    }

    /**
     * Get count of users with at least one active authority.
     */
    private Mono<Long> getActiveUsersCount() {
        String query = "SELECT COUNT(DISTINCT user_id) FROM scr_user_authority WHERE is_active = true";
        return databaseClient.sql(query).map((row, metadata) -> row.get(0, Long.class)).one().defaultIfEmpty(0L);
    }

    /**
     * Get count of expired roles that are still marked as active.
     */
    private Mono<Long> getExpiredRolesCount() {
        Instant now = Instant.now();
        String query = "SELECT COUNT(*) FROM scr_user_authority WHERE is_active = true AND expires_at IS NOT NULL AND expires_at < :now";
        return databaseClient.sql(query).bind("now", now).map((row, metadata) -> row.get(0, Long.class)).one().defaultIfEmpty(0L);
    }

    /**
     * Get recent audit activity.
     *
     * @param limit number of recent activities to retrieve
     * @return flux of recent activities
     */
    private Flux<RecentActivityDTO> getRecentActivity(int limit) {
        LOG.debug("Getting recent activity, limit: {}", limit);

        String query =
            "SELECT aa.id, aa.authority_id, a.name as authority_name, aa.action, aa.changed_by, aa.changed_date " +
            "FROM scr_authority_audit aa " +
            "LEFT JOIN scr_authority a ON aa.authority_id = a.id " +
            "ORDER BY aa.changed_date DESC " +
            "LIMIT :limit";

        return databaseClient
            .sql(query)
            .bind("limit", limit)
            .map((row, metadata) -> {
                RecentActivityDTO dto = new RecentActivityDTO();
                dto.setId(row.get("id", Long.class));
                dto.setAuthorityId(row.get("authority_id", Long.class));
                dto.setAuthorityName(row.get("authority_name", String.class));
                dto.setAction(com.tyse.scrutiny.gateway.domain.enumeration.AuditAction.valueOf(row.get("action", String.class)));
                dto.setChangedBy(row.get("changed_by", String.class));
                dto.setChangedDate(row.get("changed_date", Instant.class));

                // Generate a simple description
                String action = dto.getAction().name().toLowerCase();
                String description = dto.getChangedBy() + " " + action + " " + dto.getAuthorityName();
                dto.setDescription(description);

                return dto;
            })
            .all();
    }

    /**
     * Get roles expiring within the next N days.
     *
     * @param daysAhead number of days to look ahead
     * @return flux of expiring roles
     */
    private Flux<ExpiringRoleDTO> getExpiringRoles(int daysAhead) {
        LOG.debug("Getting roles expiring within {} days", daysAhead);

        Instant now = Instant.now();
        Instant futureDate = now.plus(daysAhead, ChronoUnit.DAYS);

        String query =
            "SELECT ua.id, ua.user_id, u.login as user_login, COALESCE(u.first_name || ' ' || u.last_name, u.login) as user_name, " +
            "ua.authority_id, a.name as authority_name, ua.expires_at " +
            "FROM scr_user_authority ua " +
            "INNER JOIN scr_user u ON ua.user_id = u.id " +
            "INNER JOIN scr_authority a ON ua.authority_id = a.id " +
            "WHERE ua.is_active = true " +
            "AND ua.expires_at IS NOT NULL " +
            "AND ua.expires_at BETWEEN :now AND :futureDate " +
            "ORDER BY ua.expires_at ASC";

        return databaseClient
            .sql(query)
            .bind("now", now)
            .bind("futureDate", futureDate)
            .map((row, metadata) -> {
                ExpiringRoleDTO dto = new ExpiringRoleDTO();
                dto.setId(row.get("id", Long.class));
                dto.setUserId(row.get("user_id", Long.class));
                dto.setUserName(row.get("user_name", String.class));
                dto.setUserLogin(row.get("user_login", String.class));
                dto.setAuthorityId(row.get("authority_id", Long.class));
                dto.setAuthorityName(row.get("authority_name", String.class));
                dto.setExpiresAt(row.get("expires_at", Instant.class));

                // Calculate days until expiration
                long days = ChronoUnit.DAYS.between(now, dto.getExpiresAt());
                dto.setDaysUntilExpiration(days);

                return dto;
            })
            .all();
    }

    /**
     * Get top authorities by user count.
     *
     * @param limit number of top authorities to retrieve
     * @return flux of authority usage
     */
    private Flux<AuthorityUsageDTO> getTopAuthorities(int limit) {
        LOG.debug("Getting top {} authorities by user count", limit);

        String query =
            "SELECT a.id, a.name, a.code, " +
            "COUNT(DISTINCT ua.user_id) as user_count, " +
            "COUNT(DISTINCT CASE WHEN ua.is_active = true THEN ua.user_id END) as active_count, " +
            "COUNT(DISTINCT CASE WHEN ua.is_active = false THEN ua.user_id END) as expired_count " +
            "FROM scr_authority a " +
            "LEFT JOIN scr_user_authority ua ON a.id = ua.authority_id " +
            "GROUP BY a.id, a.name, a.code " +
            "ORDER BY user_count DESC " +
            "LIMIT :limit";

        return databaseClient
            .sql(query)
            .bind("limit", limit)
            .map((row, metadata) -> {
                Long authorityId = row.get("id", Long.class);
                String authorityName = row.get("name", String.class);
                String authorityCode = row.get("code", String.class);
                Long userCount = row.get("user_count", Long.class);
                Long activeCount = row.get("active_count", Long.class);
                Long expiredCount = row.get("expired_count", Long.class);

                return new AuthorityUsageDTO(
                    authorityId,
                    authorityName,
                    authorityCode,
                    userCount != null ? userCount : 0L,
                    activeCount != null ? activeCount : 0L,
                    expiredCount != null ? expiredCount : 0L
                );
            })
            .all();
    }

    /**
     * Get permission usage statistics.
     *
     * @param limit number of top permissions to retrieve
     * @return flux of permission usage
     */
    private Flux<PermissionUsageDTO> getPermissionUsage(int limit) {
        LOG.debug("Getting top {} permissions by usage", limit);

        String query =
            "SELECT p.id, p.name, p.resource, p.action, " +
            "COUNT(DISTINCT ap.authority_id) as authority_count, " +
            "COUNT(DISTINCT up.user_id) as direct_user_count, " +
            "(COUNT(DISTINCT ap.authority_id) + COUNT(DISTINCT up.user_id)) as usage_count " +
            "FROM scr_permission p " +
            "LEFT JOIN scr_authority_permission ap ON p.id = ap.permission_id " +
            "LEFT JOIN scr_user_permission up ON p.id = up.permission_id " +
            "GROUP BY p.id, p.name, p.resource, p.action " +
            "ORDER BY usage_count DESC " +
            "LIMIT :limit";

        return databaseClient
            .sql(query)
            .bind("limit", limit)
            .map((row, metadata) -> {
                Long permissionId = row.get("id", Long.class);
                String permissionName = row.get("name", String.class);
                String resource = row.get("resource", String.class);
                String action = row.get("action", String.class);
                Long usageCount = row.get("usage_count", Long.class);
                Long authorityCount = row.get("authority_count", Long.class);
                Long directUserCount = row.get("direct_user_count", Long.class);

                return new PermissionUsageDTO(
                    permissionId,
                    permissionName,
                    resource,
                    action,
                    usageCount != null ? usageCount : 0L,
                    authorityCount != null ? authorityCount : 0L,
                    directUserCount != null ? directUserCount : 0L
                );
            })
            .all();
    }
}
