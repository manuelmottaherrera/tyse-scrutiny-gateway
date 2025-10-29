package com.tyse.scrutiny.gateway.repository.authorization;

import com.tyse.scrutiny.gateway.domain.authorization.UserAuthority;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the UserAuthority entity.
 * Manages user-role assignments with support for expiration and revocation.
 */
@Repository
public interface UserAuthorityRepository extends R2dbcRepository<UserAuthority, Long>, UserAuthorityRepositoryInternal {
    /**
     * Find all authority assignments for a user (active and inactive).
     */
    Flux<UserAuthority> findByUserId(Long userId);

    /**
     * Find active authority assignments for a user.
     */
    Flux<UserAuthority> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * Find all users assigned to a specific authority.
     */
    Flux<UserAuthority> findByAuthorityId(Long authorityId);

    /**
     * Find active users assigned to a specific authority.
     */
    Flux<UserAuthority> findByAuthorityIdAndIsActiveTrue(Long authorityId);

    /**
     * Find a specific user-authority assignment.
     */
    Mono<UserAuthority> findByUserIdAndAuthorityId(Long userId, Long authorityId);

    /**
     * Find assignments that were granted by a specific user.
     */
    Flux<UserAuthority> findByAssignedBy(String assignedBy);

    /**
     * Find revoked assignments.
     */
    Flux<UserAuthority> findByRevokedByIsNotNull();

    /**
     * Find assignments revoked by a specific user.
     */
    Flux<UserAuthority> findByRevokedBy(String revokedBy);

    /**
     * Delete all assignments for a user (cleanup utility).
     */
    @Query("DELETE FROM scr_user_authority WHERE user_id = :userId")
    Mono<Void> deleteByUserId(Long userId);

    /**
     * Delete all assignments for an authority (cleanup utility).
     */
    @Query("DELETE FROM scr_user_authority WHERE authority_id = :authorityId")
    Mono<Void> deleteByAuthorityId(Long authorityId);
}

/**
 * Internal interface for custom queries with complex logic.
 */
interface UserAuthorityRepositoryInternal {
    /**
     * Find valid (active + non-expired) authority assignments for a user.
     * This is the main method for checking user permissions.
     */
    Flux<UserAuthority> findValidByUserId(Long userId);

    /**
     * Find expired assignments (for cleanup/notification jobs).
     */
    Flux<UserAuthority> findExpiredAssignments();

    /**
     * Find assignments expiring soon (within next N days).
     * Useful for sending expiration warnings.
     */
    Flux<UserAuthority> findExpiringWithinDays(int days);

    /**
     * Find assignments with their authority details (join query).
     */
    Flux<UserAuthority> findByUserIdWithAuthority(Long userId);

    /**
     * Check if user has a specific authority (valid assignment).
     */
    Mono<Boolean> userHasAuthority(Long userId, Long authorityId);

    /**
     * Count active assignments for an authority.
     */
    Mono<Long> countActiveByAuthorityId(Long authorityId);
}

/**
 * Internal implementation of custom UserAuthority queries.
 */
class UserAuthorityRepositoryInternalImpl implements UserAuthorityRepositoryInternal {

    private final org.springframework.r2dbc.core.DatabaseClient db;
    private final org.springframework.data.r2dbc.convert.R2dbcConverter r2dbcConverter;

    public UserAuthorityRepositoryInternalImpl(
        org.springframework.r2dbc.core.DatabaseClient db,
        org.springframework.data.r2dbc.convert.R2dbcConverter r2dbcConverter
    ) {
        this.db = db;
        this.r2dbcConverter = r2dbcConverter;
    }

    @Override
    public Flux<UserAuthority> findValidByUserId(Long userId) {
        String sql =
            """
            SELECT *
            FROM scr_user_authority
            WHERE user_id = :userId
              AND is_active = true
              AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
            ORDER BY assigned_date DESC
            """;

        return db.sql(sql).bind("userId", userId).map((row, metadata) -> r2dbcConverter.read(UserAuthority.class, row, metadata)).all();
    }

    @Override
    public Flux<UserAuthority> findExpiredAssignments() {
        String sql =
            """
            SELECT *
            FROM scr_user_authority
            WHERE is_active = true
              AND expires_at IS NOT NULL
              AND expires_at <= CURRENT_TIMESTAMP
            ORDER BY expires_at
            """;

        return db.sql(sql).map((row, metadata) -> r2dbcConverter.read(UserAuthority.class, row, metadata)).all();
    }

    @Override
    public Flux<UserAuthority> findExpiringWithinDays(int days) {
        String sql =
            """
            SELECT *
            FROM scr_user_authority
            WHERE is_active = true
              AND expires_at IS NOT NULL
              AND expires_at > CURRENT_TIMESTAMP
              AND expires_at <= CURRENT_TIMESTAMP + INTERVAL '%d days'
            ORDER BY expires_at
            """.formatted(days);

        return db.sql(sql).map((row, metadata) -> r2dbcConverter.read(UserAuthority.class, row, metadata)).all();
    }

    @Override
    public Flux<UserAuthority> findByUserIdWithAuthority(Long userId) {
        String sql =
            """
            SELECT ua.*,
                   a.id as authority_id,
                   a.name as authority_name,
                   a.code as authority_code
            FROM scr_user_authority ua
            INNER JOIN scr_authority a ON ua.authority_id = a.id
            WHERE ua.user_id = :userId
            ORDER BY ua.assigned_date DESC
            """;

        return db.sql(sql).bind("userId", userId).map((row, metadata) -> r2dbcConverter.read(UserAuthority.class, row, metadata)).all();
    }

    @Override
    public Mono<Boolean> userHasAuthority(Long userId, Long authorityId) {
        String sql =
            """
            SELECT COUNT(*)
            FROM scr_user_authority
            WHERE user_id = :userId
              AND authority_id = :authorityId
              AND is_active = true
              AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
            """;

        return db
            .sql(sql)
            .bind("userId", userId)
            .bind("authorityId", authorityId)
            .map(row -> row.get(0, Long.class))
            .one()
            .map(count -> count > 0);
    }

    @Override
    public Mono<Long> countActiveByAuthorityId(Long authorityId) {
        String sql =
            """
            SELECT COUNT(*)
            FROM scr_user_authority
            WHERE authority_id = :authorityId
              AND is_active = true
              AND (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP)
            """;

        return db.sql(sql).bind("authorityId", authorityId).map(row -> row.get(0, Long.class)).one();
    }
}
