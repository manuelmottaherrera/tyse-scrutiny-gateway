package com.tyse.scrutiny.gateway.repository.authorization;

import com.tyse.scrutiny.gateway.domain.authorization.UserPermission;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the UserPermission entity.
 * Manages direct permission grants to users (bypassing roles) with expiration and revocation support.
 */
@Repository
public interface UserPermissionRepository extends R2dbcRepository<UserPermission, Long>, UserPermissionRepositoryInternal {
    /**
     * Find all permission grants for a user (active and inactive).
     */
    Flux<UserPermission> findByUserId(Long userId);

    /**
     * Find active permission grants for a user.
     */
    Flux<UserPermission> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find all users granted a specific permission.
     */
    Flux<UserPermission> findByPermissionId(Long permissionId);

    /**
     * Find active users granted a specific permission.
     */
    Flux<UserPermission> findByPermissionIdAndIsActiveTrue(Long permissionId);

    /**
     * Find a specific user-permission grant.
     */
    Mono<UserPermission> findByUserIdAndPermissionId(Long userId, Long permissionId);

    /**
     * Find grants that were issued by a specific user.
     */
    Flux<UserPermission> findByGrantedBy(String grantedBy);

    /**
     * Find revoked grants.
     */
    Flux<UserPermission> findByRevokedByIsNotNull();

    /**
     * Find grants revoked by a specific user.
     */
    Flux<UserPermission> findByRevokedBy(String revokedBy);

    /**
     * Find grants with a specific reason (e.g., "Emergency access", "Temporary escalation").
     */
    Flux<UserPermission> findByReason(String reason);

    /**
     * Delete all grants for a user (cleanup utility).
     */
    @Query("DELETE FROM scr_user_permission WHERE user_id = :userId")
    Mono<Void> deleteByUserId(Long userId);

    /**
     * Delete all grants for a permission (cleanup utility).
     */
    @Query("DELETE FROM scr_user_permission WHERE permission_id = :permissionId")
    Mono<Void> deleteByPermissionId(Long permissionId);
}

/**
 * Internal interface for custom queries with complex logic.
 */
interface UserPermissionRepositoryInternal {
    /**
     * Find valid (active + non-expired) permission grants for a user.
     * This is the main method for checking user permissions.
     */
    Flux<UserPermission> findValidByUserId(Long userId);

    /**
     * Find expired grants (for cleanup/notification jobs).
     */
    Flux<UserPermission> findExpiredGrants();

    /**
     * Find grants expiring soon (within next N days).
     * Useful for sending expiration warnings.
     */
    Flux<UserPermission> findExpiringWithinDays(int days);

    /**
     * Find grants with their permission details (join query).
     */
    Flux<UserPermission> findByUserIdWithPermission(Long userId);

    /**
     * Check if user has a specific permission (valid grant).
     */
    Mono<Boolean> userHasPermission(Long userId, Long permissionId);

    /**
     * Check if user has permission by name (resource.action).
     */
    Mono<Boolean> userHasPermissionByName(Long userId, String permissionName);

    /**
     * Count active grants for a permission.
     */
    Mono<Long> countActiveByPermissionId(Long permissionId);

    /**
     * Find temporary grants (those with expiration dates).
     */
    Flux<UserPermission> findTemporaryGrantsByUserId(Long userId);
}

/**
 * Internal implementation of custom UserPermission queries.
 */
class UserPermissionRepositoryInternalImpl implements UserPermissionRepositoryInternal {

    private final org.springframework.r2dbc.core.DatabaseClient db;
    private final org.springframework.data.r2dbc.convert.R2dbcConverter r2dbcConverter;

    public UserPermissionRepositoryInternalImpl(
        org.springframework.r2dbc.core.DatabaseClient db,
        org.springframework.data.r2dbc.convert.R2dbcConverter r2dbcConverter
    ) {
        this.db = db;
        this.r2dbcConverter = r2dbcConverter;
    }

    @Override
    public Flux<UserPermission> findValidByUserId(Long userId) {
        String sql =
            """
            SELECT *
            FROM scr_user_permission
            WHERE user_id = :userId
              AND is_active = true
              AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
            ORDER BY granted_date DESC
            """;

        return db.sql(sql).bind("userId", userId).map((row, metadata) -> r2dbcConverter.read(UserPermission.class, row, metadata)).all();
    }

    @Override
    public Flux<UserPermission> findExpiredGrants() {
        String sql =
            """
            SELECT *
            FROM scr_user_permission
            WHERE is_active = true
              AND expires_at IS NOT NULL
              AND expires_at <= CURRENT_TIMESTAMP
            ORDER BY expires_at
            """;

        return db.sql(sql).map((row, metadata) -> r2dbcConverter.read(UserPermission.class, row, metadata)).all();
    }

    @Override
    public Flux<UserPermission> findExpiringWithinDays(int days) {
        String sql =
            """
            SELECT *
            FROM scr_user_permission
            WHERE is_active = true
              AND expires_at IS NOT NULL
              AND expires_at > CURRENT_TIMESTAMP
              AND expires_at <= CURRENT_TIMESTAMP + INTERVAL '%d days'
            ORDER BY expires_at
            """.formatted(days);

        return db.sql(sql).map((row, metadata) -> r2dbcConverter.read(UserPermission.class, row, metadata)).all();
    }

    @Override
    public Flux<UserPermission> findByUserIdWithPermission(Long userId) {
        String sql =
            """
            SELECT up.*,
                   p.id as permission_id,
                   p.name as permission_name,
                   p.resource as permission_resource,
                   p.action as permission_action
            FROM scr_user_permission up
            INNER JOIN scr_permission p ON up.permission_id = p.id
            WHERE up.user_id = :userId
            ORDER BY up.granted_date DESC
            """;

        return db.sql(sql).bind("userId", userId).map((row, metadata) -> r2dbcConverter.read(UserPermission.class, row, metadata)).all();
    }

    @Override
    public Mono<Boolean> userHasPermission(Long userId, Long permissionId) {
        String sql =
            """
            SELECT COUNT(*)
            FROM scr_user_permission
            WHERE user_id = :userId
              AND permission_id = :permissionId
              AND is_active = true
              AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
            """;

        return db
            .sql(sql)
            .bind("userId", userId)
            .bind("permissionId", permissionId)
            .map(row -> row.get(0, Long.class))
            .one()
            .map(count -> count > 0);
    }

    @Override
    public Mono<Boolean> userHasPermissionByName(Long userId, String permissionName) {
        String sql =
            """
            SELECT COUNT(*)
            FROM scr_user_permission up
            INNER JOIN scr_permission p ON up.permission_id = p.id
            WHERE up.user_id = :userId
              AND p.name = :permissionName
              AND up.is_active = true
              AND (up.expires_at IS NULL OR up.expires_at > CURRENT_TIMESTAMP)
              AND p.is_active = true
            """;

        return db
            .sql(sql)
            .bind("userId", userId)
            .bind("permissionName", permissionName)
            .map(row -> row.get(0, Long.class))
            .one()
            .map(count -> count > 0);
    }

    @Override
    public Mono<Long> countActiveByPermissionId(Long permissionId) {
        String sql =
            """
            SELECT COUNT(*)
            FROM scr_user_permission
            WHERE permission_id = :permissionId
              AND is_active = true
              AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
            """;

        return db.sql(sql).bind("permissionId", permissionId).map(row -> row.get(0, Long.class)).one();
    }

    @Override
    public Flux<UserPermission> findTemporaryGrantsByUserId(Long userId) {
        String sql =
            """
            SELECT *
            FROM scr_user_permission
            WHERE user_id = :userId
              AND expires_at IS NOT NULL
              AND is_active = true
            ORDER BY expires_at
            """;

        return db.sql(sql).bind("userId", userId).map((row, metadata) -> r2dbcConverter.read(UserPermission.class, row, metadata)).all();
    }
}
