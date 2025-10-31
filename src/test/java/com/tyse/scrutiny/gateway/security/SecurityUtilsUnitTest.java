package com.tyse.scrutiny.gateway.security;

import static com.tyse.scrutiny.gateway.security.SecurityUtils.USER_ID_CLAIM;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.util.context.Context;

/**
 * Test class for the {@link SecurityUtils} utility class.
 */
class SecurityUtilsUnitTest {

    @Test
    void testgetCurrentUserLogin() {
        String login = SecurityUtils.getCurrentUserLogin()
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin")))
            .block();
        assertThat(login).isEqualTo("admin");
    }

    @Test
    void testgetCurrentUserJWT() {
        String jwt = SecurityUtils.getCurrentUserJWT()
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(new UsernamePasswordAuthenticationToken("admin", "token")))
            .block();
        assertThat(jwt).isEqualTo("token");
    }

    @Test
    void testGetCurrentUserId() {
        var userId = 1L;
        var now = Instant.now();
        var jwt = Jwt.withTokenValue("token")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(60))
            .claim(USER_ID_CLAIM, userId)
            .header("Test", "test")
            .build();

        var authentication = new UsernamePasswordAuthenticationToken(jwt, "token");
        var contextUserId = SecurityUtils.getCurrentUserId()
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
            .block();

        assertThat(contextUserId).isEqualTo(userId);
    }

    @Test
    void testIsAuthenticated() {
        Boolean isAuthenticated = SecurityUtils.isAuthenticated()
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin")))
            .block();
        assertThat(isAuthenticated).isTrue();
    }

    @Test
    void testAnonymousIsNotAuthenticated() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS));
        Boolean isAuthenticated = SecurityUtils.isAuthenticated()
            .contextWrite(
                ReactiveSecurityContextHolder.withAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin", authorities))
            )
            .block();
        assertThat(isAuthenticated).isFalse();
    }

    @Test
    void testHasCurrentUserAnyOfAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("admin", "admin", authorities)
        );
        Boolean hasCurrentUserThisAuthority = SecurityUtils.hasCurrentUserAnyOfAuthorities(
            AuthoritiesConstants.USER,
            AuthoritiesConstants.ADMIN
        )
            .contextWrite(context)
            .block();
        assertThat(hasCurrentUserThisAuthority).isTrue();

        hasCurrentUserThisAuthority = SecurityUtils.hasCurrentUserAnyOfAuthorities(
            AuthoritiesConstants.ANONYMOUS,
            AuthoritiesConstants.ADMIN
        )
            .contextWrite(context)
            .block();
        assertThat(hasCurrentUserThisAuthority).isFalse();
    }

    @Test
    void testHasCurrentUserNoneOfAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("admin", "admin", authorities)
        );
        Boolean hasCurrentUserThisAuthority = SecurityUtils.hasCurrentUserNoneOfAuthorities(
            AuthoritiesConstants.USER,
            AuthoritiesConstants.ADMIN
        )
            .contextWrite(context)
            .block();
        assertThat(hasCurrentUserThisAuthority).isFalse();

        hasCurrentUserThisAuthority = SecurityUtils.hasCurrentUserNoneOfAuthorities(
            AuthoritiesConstants.ANONYMOUS,
            AuthoritiesConstants.ADMIN
        )
            .contextWrite(context)
            .block();
        assertThat(hasCurrentUserThisAuthority).isTrue();
    }

    @Test
    void testHasCurrentUserThisAuthority() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("admin", "admin", authorities)
        );
        Boolean hasCurrentUserThisAuthority = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.USER)
            .contextWrite(context)
            .block();
        assertThat(hasCurrentUserThisAuthority).isTrue();

        hasCurrentUserThisAuthority = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN).contextWrite(context).block();
        assertThat(hasCurrentUserThisAuthority).isFalse();
    }

    @Test
    void testHasPermission_withValidPermission_returnsTrue() {
        // Given: usuario con permiso "user.create"
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("user.create"));
        authorities.add(new SimpleGrantedAuthority("user.read"));

        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("testuser", "password", authorities)
        );

        // When: verificar permiso "user.create"
        Boolean hasPermission = SecurityUtils.hasPermission("user.create").contextWrite(context).block();

        // Then: debe retornar true
        assertThat(hasPermission).isTrue();
    }

    @Test
    void testHasPermission_withoutPermission_returnsFalse() {
        // Given: usuario sin permiso "user.delete"
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("user.create"));

        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("testuser", "password", authorities)
        );

        // When: verificar permiso "user.delete"
        Boolean hasPermission = SecurityUtils.hasPermission("user.delete").contextWrite(context).block();

        // Then: debe retornar false
        assertThat(hasPermission).isFalse();
    }

    @Test
    void testHasAuthority_delegatesToHasPermission() {
        // Given: usuario con rol ROLE_ADMIN
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("admin", "password", authorities)
        );

        // When: verificar authority usando hasAuthority
        Boolean hasAuthority = SecurityUtils.hasAuthority("ROLE_ADMIN").contextWrite(context).block();

        // Then: debe retornar true
        assertThat(hasAuthority).isTrue();
    }

    @Test
    void testGetCurrentUserPermissions_returnsOnlyPermissions() {
        // Given: usuario con roles y permisos mezclados
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("user.create"));
        authorities.add(new SimpleGrantedAuthority("user.read"));
        authorities.add(new SimpleGrantedAuthority("invoice.approve"));

        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("testuser", "password", authorities)
        );

        // When: obtener solo permisos
        var permissions = SecurityUtils.getCurrentUserPermissions().contextWrite(context).block();

        // Then: solo debe retornar permisos (con "."), no roles
        assertThat(permissions).isNotNull();
        assertThat(permissions).hasSize(3);
        assertThat(permissions).contains("user.create", "user.read", "invoice.approve");
        assertThat(permissions).doesNotContain("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void testGetCurrentUserAuthorities_returnsOnlyRoles() {
        // Given: usuario con roles y permisos mezclados
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        authorities.add(new SimpleGrantedAuthority("user.create"));
        authorities.add(new SimpleGrantedAuthority("user.read"));
        authorities.add(new SimpleGrantedAuthority("invoice.approve"));

        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("testuser", "password", authorities)
        );

        // When: obtener solo authorities/roles
        var roles = SecurityUtils.getCurrentUserAuthorities().contextWrite(context).block();

        // Then: solo debe retornar roles (ROLE_*), no permisos
        assertThat(roles).isNotNull();
        assertThat(roles).hasSize(2);
        assertThat(roles).contains("ROLE_USER", "ROLE_ADMIN");
        assertThat(roles).doesNotContain("user.create", "user.read", "invoice.approve");
    }

    @Test
    void testGetCurrentUserPermissions_withNoPermissions_returnsEmptySet() {
        // Given: usuario solo con roles, sin permisos
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("testuser", "password", authorities)
        );

        // When: obtener permisos
        var permissions = SecurityUtils.getCurrentUserPermissions().contextWrite(context).block();

        // Then: debe retornar conjunto vacío
        assertThat(permissions).isNotNull();
        assertThat(permissions).isEmpty();
    }

    @Test
    void testGetCurrentUserAuthorities_withNoRoles_returnsEmptySet() {
        // Given: usuario solo con permisos, sin roles
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("user.create"));
        authorities.add(new SimpleGrantedAuthority("user.read"));

        Context context = ReactiveSecurityContextHolder.withAuthentication(
            new UsernamePasswordAuthenticationToken("testuser", "password", authorities)
        );

        // When: obtener roles
        var roles = SecurityUtils.getCurrentUserAuthorities().contextWrite(context).block();

        // Then: debe retornar conjunto vacío
        assertThat(roles).isNotNull();
        assertThat(roles).isEmpty();
    }
}
