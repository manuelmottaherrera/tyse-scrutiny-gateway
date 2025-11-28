package com.tyse.scrutiny.gateway.repository.authorization;

import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Permission entity.
 * Permissions follow the resource.action pattern (e.g., "user.create", "report.export").
 */
@Repository
public interface PermissionRepository extends R2dbcRepository<Permission, Long>, PermissionRepositoryInternal {
    /**
     * Find permission by unique name (resource.action).
     * Example: findByName("user.create")
     */
    Mono<Permission> findByName(String name);

    /**
     * Find all permissions for a specific resource.
     * Example: findByResource("user") returns user.create, user.read, user.update, user.delete
     */
    Flux<Permission> findByResource(String resource);

    /**
     * Find permissions by action across all resources.
     * Example: findByAction("create") returns user.create, report.create, etc.
     */
    Flux<Permission> findByAction(String action);

    /**
     * Find all active permissions.
     */
    Flux<Permission> findByIsActiveTrue();

    /**
     * Find active permissions for a specific resource.
     */
    Flux<Permission> findByResourceAndIsActiveTrue(String resource);

    /**
     * Find permission by resource and action.
     * Example: findByResourceAndAction("user", "create")
     */
    Mono<Permission> findByResourceAndAction(String resource, String action);
}

/**
 * Internal interface for custom queries that require manual implementation.
 */
interface PermissionRepositoryInternal {
    /**
     * Find permissions assigned to a specific authority (role).
     */
    Flux<Permission> findByAuthorityId(Long authorityId);

    /**
     * Find permissions granted directly to a user.
     */
    Flux<Permission> findByUserId(Long userId);

    /**
     * Find permissions for a user through their assigned roles (authorities).
     * This queries: user -> user_authority -> authority_permission -> permission
     */
    Flux<Permission> findByUserRoles(Long userId);

    /**
     * Check if a permission name is already in use.
     */
    Mono<Boolean> existsByName(String name);
}

/**
 * Internal implementation of custom Permission queries.
 */
class PermissionRepositoryInternalImpl implements PermissionRepositoryInternal {

    private final org.springframework.r2dbc.core.DatabaseClient db;
    private final org.springframework.data.r2dbc.convert.R2dbcConverter r2dbcConverter;

    public PermissionRepositoryInternalImpl(
        org.springframework.r2dbc.core.DatabaseClient db,
        org.springframework.data.r2dbc.convert.R2dbcConverter r2dbcConverter
    ) {
        this.db = db;
        this.r2dbcConverter = r2dbcConverter;
    }

    @Override
    public Flux<Permission> findByAuthorityId(Long authorityId) {
        String sql =
            """
            SELECT p.*
            FROM scr_permission p
            INNER JOIN scr_authority_permission ap ON p.id = ap.permission_id
            WHERE ap.authority_id = :authorityId AND p.is_active = true
            ORDER BY p.resource, p.action
            """;

        return db
            .sql(sql)
            .bind("authorityId", authorityId)
            .map((row, metadata) -> r2dbcConverter.read(Permission.class, row, metadata))
            .all();
    }

    @Override
    public Flux<Permission> findByUserId(Long userId) {
        String sql =
            """
            SELECT p.*
            FROM scr_permission p
            INNER JOIN scr_user_permission up ON p.id = up.permission_id
            WHERE up.user_id = :userId
              AND up.is_active = true
              AND (up.expires_at IS NULL OR up.expires_at > CURRENT_TIMESTAMP)
              AND p.is_active = true
            ORDER BY p.resource, p.action
            """;

        return db.sql(sql).bind("userId", userId).map((row, metadata) -> r2dbcConverter.read(Permission.class, row, metadata)).all();
    }

    @Override
    public Flux<Permission> findByUserRoles(Long userId) {
        String sql =
            """
            SELECT DISTINCT p.*
            FROM scr_permission p
            INNER JOIN scr_authority_permission ap ON p.id = ap.permission_id
            INNER JOIN scr_user_authority ua ON ap.authority_id = ua.authority_id
            WHERE ua.user_id = :userId
              AND ua.is_active = true
              AND (ua.expires_at IS NULL OR ua.expires_at > CURRENT_TIMESTAMP)
              AND p.is_active = true
            ORDER BY p.resource, p.action
            """;

        return db.sql(sql).bind("userId", userId).map((row, metadata) -> r2dbcConverter.read(Permission.class, row, metadata)).all();
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM scr_permission WHERE name = :name";

        return db.sql(sql).bind("name", name).map(row -> row.get(0, Long.class)).one().map(count -> count > 0);
    }
}
