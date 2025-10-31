package com.tyse.scrutiny.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Unit tests for {@link EnterprisePermissionEvaluator}.
 */
class EnterprisePermissionEvaluatorTest {

    private EnterprisePermissionEvaluator permissionEvaluator;

    @BeforeEach
    void setup() {
        permissionEvaluator = new EnterprisePermissionEvaluator();
    }

    @Test
    void hasPermission_withValidPermission_returnsTrue() {
        // Given: usuario con permiso "user.create"
        Collection<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("user.create"),
            new SimpleGrantedAuthority("user.read")
        );
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);

        // When: verificar permiso "user.create"
        boolean hasPermission = permissionEvaluator.hasPermission(auth, null, "user.create");

        // Then: debe retornar true
        assertThat(hasPermission).isTrue();
    }

    @Test
    void hasPermission_withoutPermission_returnsFalse() {
        // Given: usuario sin permiso "user.delete"
        Collection<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("user.create"),
            new SimpleGrantedAuthority("user.read")
        );
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);

        // When: verificar permiso "user.delete"
        boolean hasPermission = permissionEvaluator.hasPermission(auth, null, "user.delete");

        // Then: debe retornar false
        assertThat(hasPermission).isFalse();
    }

    @Test
    void hasPermission_withNullAuthentication_returnsFalse() {
        // When: verificar permiso con authentication null
        boolean hasPermission = permissionEvaluator.hasPermission(null, null, "user.create");

        // Then: debe retornar false
        assertThat(hasPermission).isFalse();
    }

    @Test
    void hasPermission_withNullPermission_returnsFalse() {
        // Given: usuario con permisos
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("user.create"));
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);

        // When: verificar permiso null
        boolean hasPermission = permissionEvaluator.hasPermission(auth, null, null);

        // Then: debe retornar false
        assertThat(hasPermission).isFalse();
    }

    @Test
    void hasPermission_withRoleAuthority_returnsTrue() {
        // Given: usuario con rol ROLE_ADMIN
        Collection<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_ADMIN"),
            new SimpleGrantedAuthority("user.create")
        );
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password", authorities);

        // When: verificar rol ROLE_ADMIN
        boolean hasPermission = permissionEvaluator.hasPermission(auth, null, "ROLE_ADMIN");

        // Then: debe retornar true
        assertThat(hasPermission).isTrue();
    }

    @Test
    void hasPermission_withTargetIdAndType_delegatesToMainMethod() {
        // Given: usuario con permiso "invoice.approve"
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("invoice.approve"));
        Authentication auth = new UsernamePasswordAuthenticationToken("approver", "password", authorities);

        // When: verificar con targetId y targetType
        boolean hasPermission = permissionEvaluator.hasPermission(auth, 123L, "Invoice", "invoice.approve");

        // Then: debe retornar true (delega al método principal)
        assertThat(hasPermission).isTrue();
    }

    @Test
    void hasPermission_withMultiplePermissions_checksCorrectly() {
        // Given: usuario con varios permisos
        Collection<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority("user.create"),
            new SimpleGrantedAuthority("user.read"),
            new SimpleGrantedAuthority("user.update"),
            new SimpleGrantedAuthority("invoice.approve"),
            new SimpleGrantedAuthority("report.export")
        );
        Authentication auth = new UsernamePasswordAuthenticationToken("poweruser", "password", authorities);

        // When/Then: verificar múltiples permisos
        assertThat(permissionEvaluator.hasPermission(auth, null, "user.create")).isTrue();
        assertThat(permissionEvaluator.hasPermission(auth, null, "user.read")).isTrue();
        assertThat(permissionEvaluator.hasPermission(auth, null, "user.update")).isTrue();
        assertThat(permissionEvaluator.hasPermission(auth, null, "invoice.approve")).isTrue();
        assertThat(permissionEvaluator.hasPermission(auth, null, "report.export")).isTrue();
        assertThat(permissionEvaluator.hasPermission(auth, null, "user.delete")).isFalse();
        assertThat(permissionEvaluator.hasPermission(auth, null, "invoice.create")).isFalse();
    }

    @Test
    void hasPermission_isCaseSensitive() {
        // Given: usuario con permiso "user.create" en minúsculas
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("user.create"));
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password", authorities);

        // When/Then: verificar case sensitivity
        assertThat(permissionEvaluator.hasPermission(auth, null, "user.create")).isTrue();
        assertThat(permissionEvaluator.hasPermission(auth, null, "USER.CREATE")).isFalse();
        assertThat(permissionEvaluator.hasPermission(auth, null, "User.Create")).isFalse();
    }
}
