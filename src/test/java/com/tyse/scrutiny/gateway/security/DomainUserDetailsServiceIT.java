package com.tyse.scrutiny.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.User;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.domain.authorization.UserPermission;
import com.tyse.scrutiny.gateway.repository.UserRepository;
import com.tyse.scrutiny.gateway.repository.authorization.PermissionRepository;
import com.tyse.scrutiny.gateway.repository.authorization.UserPermissionRepository;
import com.tyse.scrutiny.gateway.service.UserService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Integrations tests for {@link DomainUserDetailsService}.
 */
@IntegrationTest
class DomainUserDetailsServiceIT {

    private static final String USER_ONE_LOGIN = "test-user-one";
    private static final String USER_ONE_EMAIL = "test-user-one@localhost";
    private static final String USER_TWO_LOGIN = "test-user-two";
    private static final String USER_TWO_EMAIL = "test-user-two@localhost";
    private static final String USER_THREE_LOGIN = "test-user-three";
    private static final String USER_THREE_EMAIL = "test-user-three@localhost";
    private static final String USER_FOUR_LOGIN = "test-user-four";
    private static final String USER_FOUR_EMAIL = "test-user-four@localhost";
    private static final String USER_FIVE_LOGIN = "test-user-five";
    private static final String USER_FIVE_EMAIL = "test-user-five@localhost";
    private static final String USER_SIX_LOGIN = "test-user-six";
    private static final String USER_SIX_EMAIL = "test-user-six@localhost";
    private static final String USER_SEVEN_LOGIN = "test-user-seven";
    private static final String USER_SEVEN_EMAIL = "test-user-seven@localhost";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    @Qualifier("userDetailsService")
    private ReactiveUserDetailsService domainUserDetailsService;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserPermissionRepository userPermissionRepository;

    private Permission createPermission(String resource, String action, String name, String description) {
        Permission permission = new Permission();
        permission.setResource(resource);
        permission.setAction(action);
        permission.setName(name);
        permission.setDescription(description);
        permission.setIsActive(true);
        permission.setCreatedBy(Constants.SYSTEM);
        permission.setCreatedDate(Instant.now());
        return permission;
    }

    public User getUserOne() {
        User userOne = new User();
        userOne.setLogin(USER_ONE_LOGIN);
        userOne.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        userOne.setActivated(true);
        userOne.setEmail(USER_ONE_EMAIL);
        userOne.setFirstName("userOne");
        userOne.setLastName("doe");
        userOne.setLangKey("en");
        userOne.setCreatedBy(Constants.SYSTEM);
        return userOne;
    }

    public User getUserTwo() {
        User userTwo = new User();
        userTwo.setLogin(USER_TWO_LOGIN);
        userTwo.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        userTwo.setActivated(true);
        userTwo.setEmail(USER_TWO_EMAIL);
        userTwo.setFirstName("userTwo");
        userTwo.setLastName("doe");
        userTwo.setLangKey("en");
        userTwo.setCreatedBy(Constants.SYSTEM);
        return userTwo;
    }

    public User getUserThree() {
        User userThree = new User();
        userThree.setLogin(USER_THREE_LOGIN);
        userThree.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        userThree.setActivated(false);
        userThree.setEmail(USER_THREE_EMAIL);
        userThree.setFirstName("userThree");
        userThree.setLastName("doe");
        userThree.setLangKey("en");
        userThree.setCreatedBy(Constants.SYSTEM);
        return userThree;
    }

    @BeforeEach
    void init() {
        userRepository.save(getUserOne()).block();
        userRepository.save(getUserTwo()).block();
        userRepository.save(getUserThree()).block();
    }

    @AfterEach
    void cleanup() {
        userService.deleteUser(USER_ONE_LOGIN).block();
        userService.deleteUser(USER_TWO_LOGIN).block();
        userService.deleteUser(USER_THREE_LOGIN).block();
    }

    @Test
    void assertThatUserCanBeFoundByLogin() {
        UserDetails userDetails = domainUserDetailsService.findByUsername(USER_ONE_LOGIN).block();
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    }

    @Test
    void assertThatUserCanBeFoundByLoginIgnoreCase() {
        UserDetails userDetails = domainUserDetailsService.findByUsername(USER_ONE_LOGIN.toUpperCase(Locale.ENGLISH)).block();
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    }

    @Test
    void assertThatUserCanBeFoundByEmail() {
        UserDetails userDetails = domainUserDetailsService.findByUsername(USER_TWO_EMAIL).block();
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_TWO_LOGIN);
    }

    @Test
    void assertThatUserCanBeFoundByEmailIgnoreCase() {
        UserDetails userDetails = domainUserDetailsService.findByUsername(USER_TWO_EMAIL.toUpperCase(Locale.ENGLISH)).block();
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_TWO_LOGIN);
    }

    @Test
    void assertThatEmailIsPrioritizedOverLogin() {
        UserDetails userDetails = domainUserDetailsService.findByUsername(USER_ONE_EMAIL).block();
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(USER_ONE_LOGIN);
    }

    @Test
    void assertThatUserNotActivatedExceptionIsThrownForNotActivatedUsers() {
        assertThatExceptionOfType(UserNotActivatedException.class).isThrownBy(() ->
            domainUserDetailsService.findByUsername(USER_THREE_LOGIN).block()
        );
    }

    @Test
    void assertThatUserDetailsIncludesPermissionsFromEnterpriseSystem() {
        // Given: crear un usuario con permisos directos
        User user = new User();
        user.setLogin(USER_FOUR_LOGIN);
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail(USER_FOUR_EMAIL);
        user.setFirstName("userFour");
        user.setLastName("doe");
        user.setLangKey("en");
        user.setCreatedBy(Constants.SYSTEM);
        User savedUser = userRepository.save(user).block();

        // Crear permisos
        Permission permission1 = createPermission("user", "create", "user.create", "Create users");
        Permission savedPermission1 = permissionRepository.save(permission1).block();

        Permission permission2 = createPermission("user", "read", "user.read", "Read users");
        Permission savedPermission2 = permissionRepository.save(permission2).block();

        // Asignar permisos al usuario
        UserPermission userPermission1 = new UserPermission();
        userPermission1.setUserId(savedUser.getId());
        userPermission1.setPermissionId(savedPermission1.getId());
        userPermission1.setIsActive(true);
        userPermission1.setGrantedBy(Constants.SYSTEM);
        userPermission1.setGrantedDate(Instant.now());
        userPermissionRepository.save(userPermission1).block();

        UserPermission userPermission2 = new UserPermission();
        userPermission2.setUserId(savedUser.getId());
        userPermission2.setPermissionId(savedPermission2.getId());
        userPermission2.setIsActive(true);
        userPermission2.setGrantedBy(Constants.SYSTEM);
        userPermission2.setGrantedDate(Instant.now());
        userPermissionRepository.save(userPermission2).block();

        // When: cargar UserDetails
        UserDetails userDetails = domainUserDetailsService.findByUsername(USER_FOUR_LOGIN).block();

        // Then: debe incluir los permisos
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).isNotEmpty();

        var authorityStrings = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).toList();

        assertThat(authorityStrings).contains("user.create", "user.read");

        // Cleanup: eliminar datos de prueba
        userPermissionRepository.delete(userPermission1).block();
        userPermissionRepository.delete(userPermission2).block();
        permissionRepository.delete(savedPermission1).block();
        permissionRepository.delete(savedPermission2).block();
        userService.deleteUser(USER_FOUR_LOGIN).block();
    }

    @Test
    void assertThatUserDetailsExcludesExpiredPermissions() {
        // Given: crear usuario con permiso expirado
        User user = new User();
        user.setLogin(USER_FIVE_LOGIN);
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail(USER_FIVE_EMAIL);
        user.setFirstName("userFive");
        user.setLastName("doe");
        user.setLangKey("en");
        user.setCreatedBy(Constants.SYSTEM);
        User savedUser = userRepository.save(user).block();

        // Crear permiso
        Permission permission = createPermission("user", "delete", "user.delete", "Delete users");
        Permission savedPermission = permissionRepository.save(permission).block();

        // Asignar permiso con fecha de expiración pasada
        UserPermission expiredPermission = new UserPermission();
        expiredPermission.setUserId(savedUser.getId());
        expiredPermission.setPermissionId(savedPermission.getId());
        expiredPermission.setIsActive(true);
        expiredPermission.setGrantedBy(Constants.SYSTEM);
        expiredPermission.setGrantedDate(Instant.now().minus(10, ChronoUnit.DAYS));
        expiredPermission.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS)); // Expirado hace 1 día
        userPermissionRepository.save(expiredPermission).block();

        // When: cargar UserDetails
        UserDetails userDetails = domainUserDetailsService.findByUsername(USER_FIVE_LOGIN).block();

        // Then: NO debe incluir el permiso expirado
        assertThat(userDetails).isNotNull();

        var authorityStrings = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).toList();

        assertThat(authorityStrings).doesNotContain("user.delete");

        // Cleanup
        userPermissionRepository.delete(expiredPermission).block();
        permissionRepository.delete(savedPermission).block();
        userService.deleteUser(USER_FIVE_LOGIN).block();
    }

    @Test
    void assertThatUserDetailsExcludesInactivePermissions() {
        // Given: crear usuario con permiso inactivo
        User user = new User();
        user.setLogin(USER_SIX_LOGIN);
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail(USER_SIX_EMAIL);
        user.setFirstName("userSix");
        user.setLastName("doe");
        user.setLangKey("en");
        user.setCreatedBy(Constants.SYSTEM);
        User savedUser = userRepository.save(user).block();

        // Crear permiso
        Permission permission = createPermission("invoice", "approve", "invoice.approve", "Approve invoices");
        Permission savedPermission = permissionRepository.save(permission).block();

        // Asignar permiso inactivo
        UserPermission inactivePermission = new UserPermission();
        inactivePermission.setUserId(savedUser.getId());
        inactivePermission.setPermissionId(savedPermission.getId());
        inactivePermission.setIsActive(false); // Inactivo
        inactivePermission.setGrantedBy(Constants.SYSTEM);
        inactivePermission.setGrantedDate(Instant.now());
        userPermissionRepository.save(inactivePermission).block();

        // When: cargar UserDetails
        UserDetails userDetails = domainUserDetailsService.findByUsername(USER_SIX_LOGIN).block();

        // Then: NO debe incluir el permiso inactivo
        assertThat(userDetails).isNotNull();

        var authorityStrings = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).toList();

        assertThat(authorityStrings).doesNotContain("invoice.approve");

        // Cleanup
        userPermissionRepository.delete(inactivePermission).block();
        permissionRepository.delete(savedPermission).block();
        userService.deleteUser(USER_SIX_LOGIN).block();
    }

    @Test
    void assertThatUserDetailsIncludesRolesAndPermissions() {
        // Given: crear usuario con roles (de JHipster) y permisos
        User user = new User();
        user.setLogin(USER_SEVEN_LOGIN);
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail(USER_SEVEN_EMAIL);
        user.setFirstName("userSeven");
        user.setLastName("doe");
        user.setLangKey("en");
        user.setCreatedBy(Constants.SYSTEM);
        User savedUser = userRepository.save(user).block();

        // Crear permiso
        Permission permission = createPermission("report", "export", "report.export", "Export reports");
        Permission savedPermission = permissionRepository.save(permission).block();

        // Asignar permiso
        UserPermission userPermission = new UserPermission();
        userPermission.setUserId(savedUser.getId());
        userPermission.setPermissionId(savedPermission.getId());
        userPermission.setIsActive(true);
        userPermission.setGrantedBy(Constants.SYSTEM);
        userPermission.setGrantedDate(Instant.now());
        userPermissionRepository.save(userPermission).block();

        // When: cargar UserDetails
        UserDetails userDetails = domainUserDetailsService.findByUsername(USER_SEVEN_LOGIN).block();

        // Then: debe incluir tanto roles como permisos
        assertThat(userDetails).isNotNull();

        var authorityStrings = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).toList();

        // Verificar que incluye permisos
        assertThat(authorityStrings).contains("report.export");

        // Cleanup
        userPermissionRepository.delete(userPermission).block();
        permissionRepository.delete(savedPermission).block();
        userService.deleteUser(USER_SEVEN_LOGIN).block();
    }
}
