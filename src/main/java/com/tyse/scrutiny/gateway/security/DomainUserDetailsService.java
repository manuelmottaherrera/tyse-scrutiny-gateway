package com.tyse.scrutiny.gateway.security;

import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.User;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.repository.UserRepository;
import com.tyse.scrutiny.gateway.service.authorization.UserPermissionService;
import java.util.*;
import java.util.stream.Collectors;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements ReactiveUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserRepository userRepository;
    private final UserPermissionService userPermissionService;

    public DomainUserDetailsService(UserRepository userRepository, UserPermissionService userPermissionService) {
        this.userRepository = userRepository;
        this.userPermissionService = userPermissionService;
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<UserDetails> findByUsername(final String login) {
        LOG.debug("Authenticating {}", login);

        if (new EmailValidator().isValid(login, null)) {
            return userRepository
                .findOneWithAuthoritiesByEmailIgnoreCase(login)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User with email " + login + " was not found in the database")))
                .flatMap(user -> createSpringSecurityUser(login, user));
        }

        String lowercaseLogin = login.toLowerCase(Locale.ENGLISH);
        return userRepository
            .findOneWithAuthoritiesByLogin(lowercaseLogin)
            .switchIfEmpty(Mono.error(new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database")))
            .flatMap(user -> createSpringSecurityUser(lowercaseLogin, user));
    }

    private Mono<org.springframework.security.core.userdetails.User> createSpringSecurityUser(String lowercaseLogin, User user) {
        if (!user.isActivated()) {
            return Mono.error(new UserNotActivatedException("User " + lowercaseLogin + " was not activated"));
        }

        // Cargar permisos efectivos del usuario (directos + heredados de roles)
        return userPermissionService
            .getEffectivePermissions(user.getId())
            .map(Permission::getName) // Convertir Permission a String (ej: "user.create")
            .map(SimpleGrantedAuthority::new) // Crear GrantedAuthority para cada permiso
            .collectList() // Acumular en lista
            .map(permissionAuthorities -> {
                // Combinar authorities de roles + permisos granulares
                Set<GrantedAuthority> allAuthorities = new HashSet<>();

                // 1. Agregar roles/authorities (ROLE_ADMIN, ROLE_USER, etc.)
                allAuthorities.addAll(
                    user.getAuthorities().stream().map(Authority::getCode).map(SimpleGrantedAuthority::new).collect(Collectors.toSet())
                );

                // 2. Agregar permisos granulares (user.create, report.export, etc.)
                allAuthorities.addAll(permissionAuthorities);

                LOG.debug(
                    "User '{}' authenticated with {} authorities and {} permissions",
                    lowercaseLogin,
                    user.getAuthorities().size(),
                    permissionAuthorities.size()
                );

                return UserWithId.fromUser(user, allAuthorities);
            });
    }

    public static class UserWithId extends org.springframework.security.core.userdetails.User {

        private final Long id;

        public UserWithId(String login, String password, Collection<? extends GrantedAuthority> authorities, Long id) {
            super(login, password, authorities);
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        /**
         * Creates a UserWithId from a User entity with custom authorities.
         * This method should be used when authorities need to include both roles and permissions.
         *
         * @param user the user entity
         * @param authorities the collection of granted authorities (roles + permissions)
         * @return UserWithId instance
         */
        public static UserWithId fromUser(User user, Collection<? extends GrantedAuthority> authorities) {
            return new UserWithId(user.getLogin(), user.getPassword(), authorities, user.getId());
        }

        /**
         * Creates a UserWithId from a User entity using only the authorities from the user entity.
         * @deprecated Use {@link #fromUser(User, Collection)} to include both roles and permissions
         */
        @Deprecated
        public static UserWithId fromUser(User user) {
            return new UserWithId(
                user.getLogin(),
                user.getPassword(),
                user.getAuthorities().stream().map(Authority::getName).map(SimpleGrantedAuthority::new).toList(),
                user.getId()
            );
        }
    }
}
