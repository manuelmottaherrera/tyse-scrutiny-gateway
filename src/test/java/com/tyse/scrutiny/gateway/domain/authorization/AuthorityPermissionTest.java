package com.tyse.scrutiny.gateway.domain.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.web.rest.TestUtil;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class AuthorityPermissionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AuthorityPermission.class);
        AuthorityPermission authorityPermission1 = new AuthorityPermission();
        authorityPermission1.setId(1L);
        AuthorityPermission authorityPermission2 = new AuthorityPermission();
        authorityPermission2.setId(authorityPermission1.getId());
        assertThat(authorityPermission1).isEqualTo(authorityPermission2);
        authorityPermission2.setId(2L);
        assertThat(authorityPermission1).isNotEqualTo(authorityPermission2);
        authorityPermission1.setId(null);
        assertThat(authorityPermission1).isNotEqualTo(authorityPermission2);
    }

    @Test
    void testAuthorityPermissionCreation() {
        AuthorityPermission authorityPermission = new AuthorityPermission();
        authorityPermission.setId(1L);
        authorityPermission.setAuthorityId(10L);
        authorityPermission.setPermissionId(20L);
        authorityPermission.setGrantedBy("admin");

        Instant grantedDate = Instant.now();
        authorityPermission.setGrantedDate(grantedDate);

        assertThat(authorityPermission.getId()).isEqualTo(1L);
        assertThat(authorityPermission.getAuthorityId()).isEqualTo(10L);
        assertThat(authorityPermission.getPermissionId()).isEqualTo(20L);
        assertThat(authorityPermission.getGrantedBy()).isEqualTo("admin");
        assertThat(authorityPermission.getGrantedDate()).isEqualTo(grantedDate);
    }

    @Test
    void testAuthorityPermissionFluentAPI() {
        Instant grantedDate = Instant.now();

        AuthorityPermission authorityPermission = new AuthorityPermission()
            .id(2L)
            .authorityId(15L)
            .permissionId(25L)
            .grantedBy("system")
            .grantedDate(grantedDate);

        assertThat(authorityPermission.getId()).isEqualTo(2L);
        assertThat(authorityPermission.getAuthorityId()).isEqualTo(15L);
        assertThat(authorityPermission.getPermissionId()).isEqualTo(25L);
        assertThat(authorityPermission.getGrantedBy()).isEqualTo("system");
        assertThat(authorityPermission.getGrantedDate()).isEqualTo(grantedDate);
    }

    @Test
    void testToString() {
        AuthorityPermission authorityPermission = new AuthorityPermission();
        authorityPermission.setId(1L);
        authorityPermission.setAuthorityId(10L);
        authorityPermission.setPermissionId(20L);
        authorityPermission.setGrantedBy("admin");

        String toString = authorityPermission.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("authorityId=10");
        assertThat(toString).contains("permissionId=20");
        assertThat(toString).contains("grantedBy='admin'");
    }

    @Test
    void testDefaultGrantedDate() {
        AuthorityPermission authorityPermission = new AuthorityPermission();

        // Verify that grantedDate is set by default
        assertThat(authorityPermission.getGrantedDate()).isNotNull();
        assertThat(authorityPermission.getGrantedDate()).isBefore(Instant.now().plusSeconds(1));
    }

    @Test
    void testAuthorityPermissionMapping() {
        // Test that represents: ROLE_ADMIN has permission "user.create"
        AuthorityPermission mapping = new AuthorityPermission();
        mapping.setId(1L);
        mapping.setAuthorityId(1L); // ROLE_ADMIN id
        mapping.setPermissionId(5L); // user.create permission id
        mapping.setGrantedBy("system");

        assertThat(mapping.getAuthorityId()).isEqualTo(1L);
        assertThat(mapping.getPermissionId()).isEqualTo(5L);
        assertThat(mapping.getGrantedBy()).isEqualTo("system");
    }

    @Test
    void testMultipleMappingsForSameAuthority() {
        // ROLE_ADMIN has multiple permissions
        AuthorityPermission mapping1 = new AuthorityPermission();
        mapping1.setId(1L);
        mapping1.setAuthorityId(1L); // ROLE_ADMIN
        mapping1.setPermissionId(5L); // user.create

        AuthorityPermission mapping2 = new AuthorityPermission();
        mapping2.setId(2L);
        mapping2.setAuthorityId(1L); // Same ROLE_ADMIN
        mapping2.setPermissionId(6L); // user.delete

        assertThat(mapping1.getAuthorityId()).isEqualTo(mapping2.getAuthorityId());
        assertThat(mapping1.getPermissionId()).isNotEqualTo(mapping2.getPermissionId());
    }

    @Test
    void testMultipleMappingsForSamePermission() {
        // Multiple roles have the same permission
        AuthorityPermission mapping1 = new AuthorityPermission();
        mapping1.setId(1L);
        mapping1.setAuthorityId(1L); // ROLE_ADMIN
        mapping1.setPermissionId(10L); // user.read

        AuthorityPermission mapping2 = new AuthorityPermission();
        mapping2.setId(2L);
        mapping2.setAuthorityId(2L); // ROLE_USER
        mapping2.setPermissionId(10L); // Same user.read permission

        assertThat(mapping1.getPermissionId()).isEqualTo(mapping2.getPermissionId());
        assertThat(mapping1.getAuthorityId()).isNotEqualTo(mapping2.getAuthorityId());
    }

    @Test
    void testGrantedByAuditField() {
        AuthorityPermission systemGranted = new AuthorityPermission();
        systemGranted.setGrantedBy("system");
        assertThat(systemGranted.getGrantedBy()).isEqualTo("system");

        AuthorityPermission adminGranted = new AuthorityPermission();
        adminGranted.setGrantedBy("admin_user");
        assertThat(adminGranted.getGrantedBy()).isEqualTo("admin_user");
    }

    @Test
    void testGrantedDateTimestamp() {
        Instant before = Instant.now();
        AuthorityPermission authorityPermission = new AuthorityPermission();
        Instant after = Instant.now();

        // The default grantedDate should be between before and after
        assertThat(authorityPermission.getGrantedDate()).isBetween(before.minusSeconds(1), after.plusSeconds(1));
    }
}
