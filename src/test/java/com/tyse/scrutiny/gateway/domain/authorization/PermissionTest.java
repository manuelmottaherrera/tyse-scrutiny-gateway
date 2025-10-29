package com.tyse.scrutiny.gateway.domain.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class PermissionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Permission.class);
        Permission permission1 = new Permission();
        permission1.setId(1L);
        Permission permission2 = new Permission();
        permission2.setId(permission1.getId());
        assertThat(permission1).isEqualTo(permission2);
        permission2.setId(2L);
        assertThat(permission1).isNotEqualTo(permission2);
        permission1.setId(null);
        assertThat(permission1).isNotEqualTo(permission2);
    }

    @Test
    void testPermissionCreation() {
        Permission permission = new Permission();
        permission.setId(1L);
        permission.setName("user.create");
        permission.setResource("user");
        permission.setAction("create");
        permission.setDescription("Allows creating new users");
        permission.setIsActive(true);

        assertThat(permission.getId()).isEqualTo(1L);
        assertThat(permission.getName()).isEqualTo("user.create");
        assertThat(permission.getResource()).isEqualTo("user");
        assertThat(permission.getAction()).isEqualTo("create");
        assertThat(permission.getDescription()).isEqualTo("Allows creating new users");
        assertThat(permission.getIsActive()).isTrue();
    }

    @Test
    void testPermissionFluentAPI() {
        Permission permission = new Permission()
            .id(1L)
            .name("user.delete")
            .resource("user")
            .action("delete")
            .description("Allows deleting users")
            .isActive(false);

        assertThat(permission.getId()).isEqualTo(1L);
        assertThat(permission.getName()).isEqualTo("user.delete");
        assertThat(permission.getResource()).isEqualTo("user");
        assertThat(permission.getAction()).isEqualTo("delete");
        assertThat(permission.getIsActive()).isFalse();
    }

    @Test
    void testToString() {
        Permission permission = new Permission();
        permission.setId(1L);
        permission.setName("user.read");
        permission.setResource("user");
        permission.setAction("read");

        String toString = permission.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name='user.read'");
        assertThat(toString).contains("resource='user'");
        assertThat(toString).contains("action='read'");
    }
}
