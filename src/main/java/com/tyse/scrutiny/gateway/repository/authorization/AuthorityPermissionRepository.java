package com.tyse.scrutiny.gateway.repository.authorization;

import com.tyse.scrutiny.gateway.domain.authorization.AuthorityPermission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the AuthorityPermission entity.
 *
 * <p>This repository manages the many-to-many relationship between authorities (roles)
 * and permissions. It allows querying which permissions are assigned to which roles
 * and vice versa.
 *
 * <h3>Key Operations:</h3>
 * <ul>
 *   <li>Get all permissions for a role (e.g., what can ROLE_ADMIN do?)</li>
 *   <li>Get all roles that have a permission (e.g., who can user.delete?)</li>
 *   <li>Check if a role has a specific permission</li>
 *   <li>Remove a permission from a role</li>
 * </ul>
 */
@Repository
public interface AuthorityPermissionRepository extends R2dbcRepository<AuthorityPermission, Long>, AuthorityPermissionRepositoryInternal {
    /**
     * Find all authority-permission mappings for a specific authority (role).
     * Returns the junction table records, not the full Permission entities.
     *
     * @param authorityId the ID of the authority (role)
     * @return flux of AuthorityPermission mappings
     */
    Flux<AuthorityPermission> findByAuthorityId(Long authorityId);

    /**
     * Find all authority-permission mappings for a specific permission.
     * Returns which roles have been granted this permission.
     *
     * @param permissionId the ID of the permission
     * @return flux of AuthorityPermission mappings
     */
    Flux<AuthorityPermission> findByPermissionId(Long permissionId);

    /**
     * Check if a specific permission is already assigned to a specific authority.
     * Useful for preventing duplicate assignments.
     *
     * @param authorityId the ID of the authority (role)
     * @param permissionId the ID of the permission
     * @return true if the mapping exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM scr_authority_permission WHERE authority_id = :authorityId AND permission_id = :permissionId")
    Mono<Boolean> existsByAuthorityIdAndPermissionId(Long authorityId, Long permissionId);

    /**
     * Count how many permissions are assigned to a specific authority.
     *
     * @param authorityId the ID of the authority (role)
     * @return count of permissions
     */
    @Query("SELECT COUNT(*) FROM scr_authority_permission WHERE authority_id = :authorityId")
    Mono<Long> countByAuthorityId(Long authorityId);

    /**
     * Count how many authorities (roles) have a specific permission.
     *
     * @param permissionId the ID of the permission
     * @return count of authorities
     */
    @Query("SELECT COUNT(*) FROM scr_authority_permission WHERE permission_id = :permissionId")
    Mono<Long> countByPermissionId(Long permissionId);
}

/**
 * Internal interface for complex custom queries that require manual implementation.
 */
interface AuthorityPermissionRepositoryInternal {
    /**
     * Find all AuthorityPermission mappings for multiple authorities at once.
     * Useful for batch operations or getting permissions for all user roles.
     *
     * @param authorityIds list of authority IDs
     * @return flux of AuthorityPermission mappings
     */
    Flux<AuthorityPermission> findByAuthorityIdIn(Iterable<Long> authorityIds);

    /**
     * Get detailed statistics about permission assignments.
     * Returns a map of authority names to permission counts.
     *
     * @return flux of tuples (authority_name, permission_count)
     */
    Flux<AuthorityPermissionStats> getPermissionStatsByAuthority();

    /**
     * Delete a specific authority-permission mapping.
     * This revokes a permission from a role.
     *
     * @param authorityId the ID of the authority (role)
     * @param permissionId the ID of the permission
     * @return number of deleted rows (0 or 1)
     */
    Mono<Long> deleteByAuthorityIdAndPermissionId(Long authorityId, Long permissionId);

    /**
     * Delete all permission mappings for a specific authority.
     * Useful when deactivating or deleting a role.
     *
     * @param authorityId the ID of the authority (role)
     * @return number of deleted rows
     */
    Mono<Long> deleteByAuthorityId(Long authorityId);

    /**
     * Delete all authority mappings for a specific permission.
     * Useful when deactivating or deleting a permission.
     *
     * @param permissionId the ID of the permission
     * @return number of deleted rows
     */
    Mono<Long> deleteByPermissionId(Long permissionId);
}

/**
 * Internal implementation of complex custom queries.
 */
class AuthorityPermissionRepositoryInternalImpl implements AuthorityPermissionRepositoryInternal {

    private final org.springframework.r2dbc.core.DatabaseClient db;
    private final org.springframework.data.r2dbc.convert.R2dbcConverter r2dbcConverter;

    public AuthorityPermissionRepositoryInternalImpl(
        org.springframework.r2dbc.core.DatabaseClient db,
        org.springframework.data.r2dbc.convert.R2dbcConverter r2dbcConverter
    ) {
        this.db = db;
        this.r2dbcConverter = r2dbcConverter;
    }

    @Override
    public Flux<AuthorityPermission> findByAuthorityIdIn(Iterable<Long> authorityIds) {
        // Convert Iterable to comma-separated string for SQL IN clause
        StringBuilder idsBuilder = new StringBuilder();
        authorityIds.forEach(id -> {
            if (idsBuilder.length() > 0) idsBuilder.append(",");
            idsBuilder.append(id);
        });

        if (idsBuilder.length() == 0) {
            return Flux.empty();
        }

        String sql =
            """
            SELECT ap.*
            FROM scr_authority_permission ap
            WHERE ap.authority_id IN (%s)
            ORDER BY ap.authority_id, ap.permission_id
            """.formatted(idsBuilder.toString());

        return db.sql(sql).map((row, metadata) -> r2dbcConverter.read(AuthorityPermission.class, row, metadata)).all();
    }

    @Override
    public Flux<AuthorityPermissionStats> getPermissionStatsByAuthority() {
        String sql =
            """
            SELECT
                a.code as authority_code,
                a.name as authority_name,
                COUNT(ap.permission_id) as permission_count
            FROM scr_authority a
            LEFT JOIN scr_authority_permission ap ON a.id = ap.authority_id
            WHERE a.is_active = true
            GROUP BY a.id, a.code, a.name
            ORDER BY permission_count DESC, a.name
            """;

        return db
            .sql(sql)
            .map((row, metadata) ->
                new AuthorityPermissionStats(
                    row.get("authority_code", String.class),
                    row.get("authority_name", String.class),
                    row.get("permission_count", Long.class)
                )
            )
            .all();
    }

    @Override
    public Mono<Long> deleteByAuthorityIdAndPermissionId(Long authorityId, Long permissionId) {
        return db
            .sql("DELETE FROM scr_authority_permission WHERE authority_id = :authorityId AND permission_id = :permissionId")
            .bind("authorityId", authorityId)
            .bind("permissionId", permissionId)
            .fetch()
            .rowsUpdated()
            .map(Long::valueOf);
    }

    @Override
    public Mono<Long> deleteByAuthorityId(Long authorityId) {
        return db
            .sql("DELETE FROM scr_authority_permission WHERE authority_id = :authorityId")
            .bind("authorityId", authorityId)
            .fetch()
            .rowsUpdated()
            .map(Long::valueOf);
    }

    @Override
    public Mono<Long> deleteByPermissionId(Long permissionId) {
        return db
            .sql("DELETE FROM scr_authority_permission WHERE permission_id = :permissionId")
            .bind("permissionId", permissionId)
            .fetch()
            .rowsUpdated()
            .map(Long::valueOf);
    }
}

/**
 * DTO for permission statistics per authority.
 */
record AuthorityPermissionStats(String authorityCode, String authorityName, Long permissionCount) {}
