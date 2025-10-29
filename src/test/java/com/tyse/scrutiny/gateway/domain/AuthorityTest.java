package com.tyse.scrutiny.gateway.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory;
import com.tyse.scrutiny.gateway.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AuthorityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Authority.class);
        Authority authority1 = new Authority();
        authority1.setId(1L);
        Authority authority2 = new Authority();
        authority2.setId(authority1.getId());
        assertThat(authority1).isEqualTo(authority2);
        authority2.setId(2L);
        assertThat(authority1).isNotEqualTo(authority2);
        authority1.setId(null);
        assertThat(authority1).isNotEqualTo(authority2);
    }

    @Test
    void testAuthorityCreation() {
        Authority authority = new Authority();
        authority.setId(1L);
        authority.setName("Administrator");
        authority.setCode("ROLE_ADMIN");
        authority.setDescription("System administrator");
        authority.setCategory(AuthorityCategory.SYSTEM);
        authority.setIsSystem(true);
        authority.setIsActive(true);
        authority.setHierarchyLevel(0);

        assertThat(authority.getId()).isEqualTo(1L);
        assertThat(authority.getName()).isEqualTo("Administrator");
        assertThat(authority.getCode()).isEqualTo("ROLE_ADMIN");
        assertThat(authority.getDescription()).isEqualTo("System administrator");
        assertThat(authority.getCategory()).isEqualTo(AuthorityCategory.SYSTEM);
        assertThat(authority.getIsSystem()).isTrue();
        assertThat(authority.getIsActive()).isTrue();
        assertThat(authority.getHierarchyLevel()).isEqualTo(0);
    }

    @Test
    void testAuthorityFluentAPI() {
        Authority authority = new Authority()
            .id(2L)
            .name("User")
            .code("ROLE_USER")
            .description("Standard user")
            .category(AuthorityCategory.SYSTEM)
            .isSystem(true)
            .isActive(true)
            .hierarchyLevel(500);

        assertThat(authority.getId()).isEqualTo(2L);
        assertThat(authority.getName()).isEqualTo("User");
        assertThat(authority.getCode()).isEqualTo("ROLE_USER");
        assertThat(authority.getHierarchyLevel()).isEqualTo(500);
    }

    @Test
    void testDefaultValues() {
        Authority authority = new Authority();

        assertThat(authority.getCategory()).isEqualTo(AuthorityCategory.CUSTOM);
        assertThat(authority.getIsSystem()).isFalse();
        assertThat(authority.getIsActive()).isTrue();
        assertThat(authority.getHierarchyLevel()).isEqualTo(1000);
    }

    @Test
    void testToString() {
        Authority authority = new Authority();
        authority.setId(1L);
        authority.setName("Manager");
        authority.setCode("ROLE_MANAGER");

        String toString = authority.toString();
        assertThat(toString).contains("id=1");
        assertThat(toString).contains("name='Manager'");
        assertThat(toString).contains("code='ROLE_MANAGER'");
    }
}
