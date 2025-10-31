package com.tyse.scrutiny.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Unit tests for {@link RequiresPermission} annotation.
 */
class RequiresPermissionTest {

    /**
     * Test service to demonstrate usage of @RequiresPermission annotation.
     */
    static class TestService {

        @RequiresPermission("user.create")
        public void createUser() {
            // Method body
        }

        @RequiresPermission("user.delete")
        public void deleteUser() {
            // Method body
        }

        @RequiresPermission("invoice.approve")
        public void approveInvoice() {
            // Method body
        }
    }

    @Test
    void annotationShouldBePresent() throws NoSuchMethodException {
        // Given: un método anotado con @RequiresPermission
        Method method = TestService.class.getDeclaredMethod("createUser");

        // When: verificar si la anotación está presente
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);

        // Then: la anotación debe estar presente
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("user.create");
    }

    @Test
    void annotationShouldContainPreAuthorize() throws NoSuchMethodException {
        // Given: la anotación @RequiresPermission
        RequiresPermission annotation = RequiresPermission.class.getAnnotation(RequiresPermission.class);

        // When: verificar si @RequiresPermission está anotada con @PreAuthorize
        PreAuthorize preAuthorize = RequiresPermission.class.getAnnotation(PreAuthorize.class);

        // Then: @RequiresPermission debe estar anotada con @PreAuthorize (meta-anotación)
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).contains("hasPermission");
    }

    @Test
    void annotationShouldHaveCorrectValue() throws NoSuchMethodException {
        // Given: diferentes métodos con diferentes permisos
        Method createMethod = TestService.class.getDeclaredMethod("createUser");
        Method deleteMethod = TestService.class.getDeclaredMethod("deleteUser");
        Method approveMethod = TestService.class.getDeclaredMethod("approveInvoice");

        // When/Then: verificar que cada método tiene el permiso correcto
        assertThat(createMethod.getAnnotation(RequiresPermission.class).value()).isEqualTo("user.create");
        assertThat(deleteMethod.getAnnotation(RequiresPermission.class).value()).isEqualTo("user.delete");
        assertThat(approveMethod.getAnnotation(RequiresPermission.class).value()).isEqualTo("invoice.approve");
    }

    @Test
    void annotationShouldBeApplicableToMethods() throws NoSuchMethodException {
        // Given: un método anotado con @RequiresPermission
        Method method = TestService.class.getDeclaredMethod("createUser");
        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);

        // Then: debe ser aplicable a métodos y tener el valor correcto
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("user.create");
    }

    /**
     * Test class to demonstrate usage at class level.
     */
    @RequiresPermission("admin.access")
    static class TestClassLevelService {

        public void someMethod() {
            // All methods in this class require "admin.access" permission
        }
    }

    @Test
    void annotationShouldBeApplicableToClasses() {
        // Given: una clase anotada con @RequiresPermission
        RequiresPermission annotation = TestClassLevelService.class.getAnnotation(RequiresPermission.class);

        // Then: la anotación debe estar presente a nivel de clase
        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("admin.access");
    }
}
