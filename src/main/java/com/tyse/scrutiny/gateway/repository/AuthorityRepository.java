package com.tyse.scrutiny.gateway.repository;

import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data R2DBC repository for the Authority entity.
 * Migrated from String-based ID to Long-based ID in Fase 3.
 */
@Repository
public interface AuthorityRepository extends R2dbcRepository<Authority, Long>, AuthorityRepositoryInternal {
    /**
     * Find authority by unique code.
     * Example: findByCode("ROLE_ADMIN")
     */
    Mono<Authority> findByCode(String code);

    /**
     * Find all active authorities.
     */
    Flux<Authority> findByIsActiveTrue();

    /**
     * Find active authorities by category.
     */
    Flux<Authority> findByCategoryAndIsActiveTrue(AuthorityCategory category);

    /**
     * Find system authorities (protected roles).
     */
    Flux<Authority> findByIsSystemTrue();

    /**
     * Find authorities by hierarchy level (for role comparison).
     */
    Flux<Authority> findByHierarchyLevelLessThanEqual(Integer level);
}

/**
 * Internal interface for custom queries that require manual implementation.
 */
interface AuthorityRepositoryInternal {
    /**
     * Find authorities with their assigned user count.
     */
    Flux<Authority> findAllWithUserCount();

    /**
     * Check if an authority code is already in use.
     */
    Mono<Boolean> existsByCode(String code);
}

/**
 * Internal implementation of custom Authority queries.
 */
class AuthorityRepositoryInternalImpl implements AuthorityRepositoryInternal {

    private final org.springframework.r2dbc.core.DatabaseClient db;
    private final org.springframework.data.r2dbc.convert.R2dbcConverter r2dbcConverter;

    public AuthorityRepositoryInternalImpl(
        org.springframework.r2dbc.core.DatabaseClient db,
        org.springframework.data.r2dbc.convert.R2dbcConverter r2dbcConverter
    ) {
        this.db = db;
        this.r2dbcConverter = r2dbcConverter;
    }

    @Override
    public Flux<Authority> findAllWithUserCount() {
        String sql =
            """
            SELECT a.*, COUNT(ua.id) as user_count
            FROM scr_authority a
            LEFT JOIN scr_user_authority ua ON a.id = ua.authority_id AND ua.is_active = true
            GROUP BY a.id
            ORDER BY a.hierarchy_level, a.name
            """;

        return db.sql(sql).map((row, metadata) -> r2dbcConverter.read(Authority.class, row, metadata)).all();
    }

    @Override
    public Mono<Boolean> existsByCode(String code) {
        String sql = "SELECT COUNT(*) FROM scr_authority WHERE code = :code";

        return db.sql(sql).bind("code", code).map(row -> row.get(0, Long.class)).one().map(count -> count > 0);
    }
}
