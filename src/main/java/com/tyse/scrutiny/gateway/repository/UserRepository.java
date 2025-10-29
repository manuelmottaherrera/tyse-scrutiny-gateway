package com.tyse.scrutiny.gateway.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.BeanComparator;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Spring Data R2DBC repository for the {@link User} entity.
 */
@Repository
public interface UserRepository extends R2dbcRepository<User, Long>, UserRepositoryInternal {
    Mono<User> findOneByActivationKey(String activationKey);

    Flux<User> findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(LocalDateTime dateTime);

    Mono<User> findOneByResetKey(String resetKey);

    Mono<User> findOneByEmailIgnoreCase(String email);

    Mono<User> findOneByLogin(String login);

    Flux<User> findAllByIdNotNull(Pageable pageable);

    Flux<User> findAllByIdNotNullAndActivatedIsTrue(Pageable pageable);

    Mono<Long> count();

    /**
     * Save user-authority assignment to the new scr_user_authority table.
     * Note: This is a legacy compatibility method. Use UserAuthorityRepository for new code.
     * @deprecated Use {@link com.tyse.scrutiny.gateway.repository.authorization.UserAuthorityRepository} instead
     */
    @Deprecated
    @Query(
        "INSERT INTO scr_user_authority (user_id, authority_id, is_active, assigned_by, assigned_date) " +
        "SELECT :userId, a.id, true, :assignedBy, CURRENT_TIMESTAMP FROM scr_authority a WHERE a.code = :authorityCode"
    )
    Mono<Void> saveUserAuthority(Long userId, String authorityCode, String assignedBy);

    /**
     * Delete all user-authority assignments (cleanup utility).
     * @deprecated Use {@link com.tyse.scrutiny.gateway.repository.authorization.UserAuthorityRepository#deleteAll()} instead
     */
    @Deprecated
    @Query("DELETE FROM scr_user_authority")
    Mono<Void> deleteAllUserAuthorities();

    /**
     * Delete all authority assignments for a specific user.
     * @deprecated Use {@link com.tyse.scrutiny.gateway.repository.authorization.UserAuthorityRepository#deleteByUserId(Long)} instead
     */
    @Deprecated
    @Query("DELETE FROM scr_user_authority WHERE user_id = :userId")
    Mono<Void> deleteUserAuthorities(Long userId);
}

interface DeleteExtended<T> {
    Mono<Void> delete(T user);
}

interface UserRepositoryInternal extends DeleteExtended<User> {
    Mono<User> findOneWithAuthoritiesByLogin(String login);

    Mono<User> findOneWithAuthoritiesByEmailIgnoreCase(String email);

    Flux<User> findAllWithAuthorities(Pageable pageable);
}

class UserRepositoryInternalImpl implements UserRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final R2dbcConverter r2dbcConverter;

    public UserRepositoryInternalImpl(DatabaseClient db, R2dbcEntityTemplate r2dbcEntityTemplate, R2dbcConverter r2dbcConverter) {
        this.db = db;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
        this.r2dbcConverter = r2dbcConverter;
    }

    @Override
    public Mono<User> findOneWithAuthoritiesByLogin(String login) {
        return findOneWithAuthoritiesBy("login", login);
    }

    @Override
    public Mono<User> findOneWithAuthoritiesByEmailIgnoreCase(String email) {
        return findOneWithAuthoritiesBy("email", email.toLowerCase());
    }

    @Override
    public Flux<User> findAllWithAuthorities(Pageable pageable) {
        String property = pageable.getSort().stream().map(Sort.Order::getProperty).findFirst().orElse("id");
        String direction = String.valueOf(
            pageable.getSort().stream().map(Sort.Order::getDirection).findFirst().orElse(Sort.DEFAULT_DIRECTION)
        );
        long page = pageable.getPageNumber();
        long size = pageable.getPageSize();

        return db
            .sql(
                """
                SELECT u.*, a.id as authority_id, a.code as authority_code, a.name as authority_name
                FROM jhi_user u
                LEFT JOIN scr_user_authority ua ON u.id = ua.user_id AND ua.is_active = true
                  AND (ua.expires_at IS NULL OR ua.expires_at > CURRENT_TIMESTAMP)
                LEFT JOIN scr_authority a ON ua.authority_id = a.id
                """
            )
            .map((row, metadata) ->
                Tuples.of(
                    r2dbcConverter.read(User.class, row, metadata),
                    Optional.ofNullable(row.get("authority_id", Long.class)),
                    Optional.ofNullable(row.get("authority_code", String.class))
                )
            )
            .all()
            .groupBy(t -> t.getT1().getLogin())
            .flatMap(l -> l.collectList().map(t -> updateUserWithAuthorities(t.get(0).getT1(), t)))
            .sort(
                Sort.Direction.fromString(direction) == Sort.DEFAULT_DIRECTION
                    ? new BeanComparator<>(property)
                    : new BeanComparator<>(property).reversed()
            )
            .skip(page * size)
            .take(size);
    }

    @Override
    public Mono<Void> delete(User user) {
        return db
            .sql("DELETE FROM scr_user_authority WHERE user_id = :userId")
            .bind("userId", user.getId())
            .then()
            .then(db.sql("DELETE FROM scr_user_permission WHERE user_id = :userId").bind("userId", user.getId()).then())
            .then(r2dbcEntityTemplate.delete(User.class).matching(query(where("id").is(user.getId()))).all().then());
    }

    private Mono<User> findOneWithAuthoritiesBy(String fieldName, Object fieldValue) {
        String sql =
            """
            SELECT u.*, a.id as authority_id, a.code as authority_code, a.name as authority_name
            FROM jhi_user u
            LEFT JOIN scr_user_authority ua ON u.id = ua.user_id AND ua.is_active = true
              AND (ua.expires_at IS NULL OR ua.expires_at > CURRENT_TIMESTAMP)
            LEFT JOIN scr_authority a ON ua.authority_id = a.id
            WHERE u.%s = :%s
            """.formatted(fieldName, fieldName);

        return db
            .sql(sql)
            .bind(fieldName, fieldValue)
            .map((row, metadata) ->
                Tuples.of(
                    r2dbcConverter.read(User.class, row, metadata),
                    Optional.ofNullable(row.get("authority_id", Long.class)),
                    Optional.ofNullable(row.get("authority_code", String.class))
                )
            )
            .all()
            .collectList()
            .filter(l -> !l.isEmpty())
            .map(l -> updateUserWithAuthorities(l.get(0).getT1(), l));
    }

    private User updateUserWithAuthorities(User user, List<reactor.util.function.Tuple3<User, Optional<Long>, Optional<String>>> tuples) {
        user.setAuthorities(
            tuples
                .stream()
                .filter(t -> t.getT2().isPresent() && t.getT3().isPresent())
                .map(t -> {
                    Authority authority = new Authority();
                    authority.setId(t.getT2().orElseThrow());
                    authority.setCode(t.getT3().orElseThrow());
                    // For backward compatibility, set name to code
                    // TODO: In Phase 5, load full authority details or use proper DTO
                    authority.setName(t.getT3().orElseThrow());
                    return authority;
                })
                .collect(Collectors.toSet())
        );

        return user;
    }
}
