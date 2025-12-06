package com.tyse.scrutiny.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyse.scrutiny.gateway.IntegrationTest;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.Authority;
import com.tyse.scrutiny.gateway.domain.User;
import com.tyse.scrutiny.gateway.domain.authorization.AuthorityPermission;
import com.tyse.scrutiny.gateway.domain.authorization.Permission;
import com.tyse.scrutiny.gateway.repository.AuthorityRepository;
import com.tyse.scrutiny.gateway.repository.UserRepository;
import com.tyse.scrutiny.gateway.repository.authorization.AuthorityPermissionRepository;
import com.tyse.scrutiny.gateway.repository.authorization.PermissionRepository;
import com.tyse.scrutiny.gateway.repository.authorization.UserAuthorityRepository;
import com.tyse.scrutiny.gateway.security.AuthoritiesConstants;
import com.tyse.scrutiny.gateway.service.UserService;
import com.tyse.scrutiny.gateway.service.dto.AdminUserDTO;
import com.tyse.scrutiny.gateway.service.dto.PasswordChangeDTO;
import com.tyse.scrutiny.gateway.web.rest.vm.KeyAndPasswordVM;
import com.tyse.scrutiny.gateway.web.rest.vm.ManagedUserVM;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link AccountResource} REST controller.
 */
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_TIMEOUT)
@IntegrationTest
class AccountResourceIT {

    static final String TEST_USER_LOGIN = "test";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private UserAuthorityRepository userAuthorityRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AuthorityPermissionRepository authorityPermissionRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebTestClient accountWebTestClient;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        // Ensure USER authority exists for registration tests
        Authority userAuth = authorityRepository
            .findByCode(AuthoritiesConstants.USER)
            .switchIfEmpty(
                Mono.defer(() -> {
                    Authority userAuthority = new Authority();
                    userAuthority.setName("User");
                    userAuthority.setCode(AuthoritiesConstants.USER);
                    userAuthority.setDescription("User role");
                    userAuthority.setCategory(com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory.SYSTEM);
                    userAuthority.setIsSystem(true);
                    userAuthority.setIsActive(true);
                    userAuthority.setHierarchyLevel(500);
                    userAuthority.setCreatedBy(Constants.SYSTEM);
                    userAuthority.setCreatedDate(Instant.now());
                    return authorityRepository.save(userAuthority);
                })
            )
            .block();

        // Ensure ADMIN authority exists for some tests
        Authority adminAuth = authorityRepository
            .findByCode(AuthoritiesConstants.ADMIN)
            .switchIfEmpty(
                Mono.defer(() -> {
                    Authority adminAuthority = new Authority();
                    adminAuthority.setName("Admin");
                    adminAuthority.setCode(AuthoritiesConstants.ADMIN);
                    adminAuthority.setDescription("Admin role");
                    adminAuthority.setCategory(com.tyse.scrutiny.gateway.domain.enumeration.AuthorityCategory.SYSTEM);
                    adminAuthority.setIsSystem(true);
                    adminAuthority.setIsActive(true);
                    adminAuthority.setHierarchyLevel(0);
                    adminAuthority.setCreatedBy(Constants.SYSTEM);
                    adminAuthority.setCreatedDate(Instant.now());
                    return authorityRepository.save(adminAuthority);
                })
            )
            .block();

        // Create permissions if they don't exist and assign them to authorities
        // These permissions are needed for testGetExistingAccountIncludesPermissions and testGetExistingAccountUserRoleHasPermissions
        ensurePermissionsExist(adminAuth, userAuth);
    }

    /**
     * Ensures that the required permissions exist and are assigned to the appropriate authorities.
     * This method is idempotent - it won't create duplicates if permissions already exist.
     */
    private void ensurePermissionsExist(Authority adminAuth, Authority userAuth) {
        // Define permissions needed for the tests
        String[][] permissionDefs = {
            { "user.read", "user", "read", "View user information" },
            { "user.create", "user", "create", "Create new users" },
            { "authority.read", "authority", "read", "View authority information" },
            { "permission.read", "permission", "read", "View permission information" },
            { "divipol.read", "divipol", "read", "View divipol data" },
            { "statistics.read", "statistics", "read", "View statistics" },
        };

        for (String[] def : permissionDefs) {
            Permission perm = permissionRepository
                .findByName(def[0])
                .switchIfEmpty(
                    Mono.defer(() -> {
                        Permission p = new Permission();
                        p.setName(def[0]);
                        p.setResource(def[1]);
                        p.setAction(def[2]);
                        p.setDescription(def[3]);
                        p.setIsActive(true);
                        p.setCreatedBy(Constants.SYSTEM);
                        p.setCreatedDate(Instant.now());
                        return permissionRepository.save(p);
                    })
                )
                .block();

            // Assign all permissions to ADMIN
            if (adminAuth != null && perm != null) {
                assignPermissionToAuthorityIfNotExists(adminAuth.getId(), perm.getId());
            }

            // Assign user.read, authority.read, permission.read to USER
            if (userAuth != null && perm != null) {
                if ("user.read".equals(def[0]) || "authority.read".equals(def[0]) || "permission.read".equals(def[0])) {
                    assignPermissionToAuthorityIfNotExists(userAuth.getId(), perm.getId());
                }
            }
        }
    }

    /**
     * Assigns a permission to an authority if the relationship doesn't already exist.
     */
    private void assignPermissionToAuthorityIfNotExists(Long authorityId, Long permissionId) {
        Boolean exists = authorityPermissionRepository.existsByAuthorityIdAndPermissionId(authorityId, permissionId).block();

        if (Boolean.FALSE.equals(exists)) {
            AuthorityPermission ap = new AuthorityPermission();
            ap.setAuthorityId(authorityId);
            ap.setPermissionId(permissionId);
            ap.setGrantedBy(Constants.SYSTEM);
            ap.setGrantedDate(Instant.now());
            authorityPermissionRepository.save(ap).block();
        }
    }

    @AfterEach
    void cleanupAndCheck() {
        // Delete user-authority relationships first to avoid foreign key constraint violations
        userAuthorityRepository.deleteAll().block();
        // Now safe to delete users
        userRepository.deleteAll().block();
    }

    @Test
    @WithUnauthenticatedMockUser
    void testNonAuthenticatedUser() {
        accountWebTestClient.get().uri("/api/authenticate").exchange().expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(TEST_USER_LOGIN)
    void testAuthenticatedUser() {
        accountWebTestClient.get().uri("/api/authenticate").exchange().expectStatus().isNoContent();
    }

    @Test
    @WithMockUser(TEST_USER_LOGIN)
    void testGetExistingAccount() {
        Set<String> authorities = new HashSet<>();
        authorities.add(AuthoritiesConstants.ADMIN);

        AdminUserDTO user = new AdminUserDTO();
        user.setLogin(TEST_USER_LOGIN);
        user.setFirstName("john");
        user.setLastName("doe");
        user.setEmail("john.doe@jhipster.com");
        user.setImageUrl("http://placehold.it/50x50");
        user.setLangKey("en");
        user.setAuthorities(authorities);
        userService.createUser(user).block();

        accountWebTestClient
            .get()
            .uri("/api/account")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.login")
            .isEqualTo(TEST_USER_LOGIN)
            .jsonPath("$.firstName")
            .isEqualTo("john")
            .jsonPath("$.lastName")
            .isEqualTo("doe")
            .jsonPath("$.email")
            .isEqualTo("john.doe@jhipster.com")
            .jsonPath("$.imageUrl")
            .isEqualTo("http://placehold.it/50x50")
            .jsonPath("$.langKey")
            .isEqualTo("en")
            .jsonPath("$.authorities[0]")
            .isEqualTo(AuthoritiesConstants.ADMIN);

        userService.deleteUser(TEST_USER_LOGIN).block();
    }

    @Test
    @WithMockUser("test-admin-permissions")
    void testGetExistingAccountIncludesPermissions() {
        // Given: Create user with ADMIN role (which has all permissions via Liquibase migrations)
        // ROLE_ADMIN has 47 permissions assigned including divipol.read, statistics.read, etc.
        Set<String> authorities = new HashSet<>();
        authorities.add(AuthoritiesConstants.ADMIN);

        AdminUserDTO user = new AdminUserDTO();
        user.setLogin("test-admin-permissions");
        user.setFirstName("Admin");
        user.setLastName("WithPermissions");
        user.setEmail("test-admin-permissions@jhipster.com");
        user.setLangKey("en");
        user.setAuthorities(authorities);
        userService.createUser(user).block();

        // When: Request account information
        // Then: Response includes permissions array with permissions from ROLE_ADMIN
        accountWebTestClient
            .get()
            .uri("/api/account")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.login")
            .isEqualTo("test-admin-permissions")
            .jsonPath("$.permissions")
            .isArray()
            .jsonPath("$.permissions")
            .value(permissions -> {
                @SuppressWarnings("unchecked")
                List<String> permList = (List<String>) permissions;
                // ROLE_ADMIN has all 47 permissions, verify some key ones
                assertThat(permList).isNotEmpty();
                assertThat(permList).contains("divipol.read", "statistics.read", "user.create", "user.read");
            });

        // Cleanup
        userService.deleteUser("test-admin-permissions").block();
    }

    @Test
    @WithMockUser("test-user-with-permissions")
    void testGetExistingAccountUserRoleHasPermissions() {
        // Given: Create user with USER role (which HAS some permissions assigned by Liquibase)
        Set<String> authorities = new HashSet<>();
        authorities.add(AuthoritiesConstants.USER);

        AdminUserDTO user = new AdminUserDTO();
        user.setLogin("test-user-with-permissions");
        user.setFirstName("User");
        user.setLastName("WithPermissions");
        user.setEmail("test-user-permissions@jhipster.com");
        user.setLangKey("en");
        user.setAuthorities(authorities);
        userService.createUser(user).block();

        // When: Request account information
        // Then: Response includes permissions array with the permissions from ROLE_USER
        // Note: ROLE_USER has user.read, authority.read, permission.read assigned via Liquibase
        accountWebTestClient
            .get()
            .uri("/api/account")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.login")
            .isEqualTo("test-user-with-permissions")
            .jsonPath("$.permissions")
            .isArray()
            .jsonPath("$.permissions")
            .value(permissions -> {
                @SuppressWarnings("unchecked")
                List<String> permList = (List<String>) permissions;
                // ROLE_USER has basic read permissions
                assertThat(permList).containsAll(List.of("user.read", "authority.read", "permission.read"));
            });

        // Cleanup
        userService.deleteUser("test-user-with-permissions").block();
    }

    @Test
    void testGetUnknownAccount() {
        accountWebTestClient
            .get()
            .uri("/api/account")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testRegisterValid() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setLogin("test-register-valid");
        validUser.setPassword("password");
        validUser.setFirstName("Alice");
        validUser.setLastName("Test");
        validUser.setEmail("test-register-valid@example.com");
        validUser.setImageUrl("http://placehold.it/50x50");
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        assertThat(userRepository.findOneByLogin("test-register-valid").blockOptional()).isEmpty();

        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(validUser))
            .exchange()
            .expectStatus()
            .isCreated();

        assertThat(userRepository.findOneByLogin("test-register-valid").blockOptional()).isPresent();

        userService.deleteUser("test-register-valid").block();
    }

    @Test
    void testRegisterInvalidLogin() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("funky-log(n"); // <-- invalid
        invalidUser.setPassword("password");
        invalidUser.setFirstName("Funky");
        invalidUser.setLastName("One");
        invalidUser.setEmail("funky@example.com");
        invalidUser.setActivated(true);
        invalidUser.setImageUrl("http://placehold.it/50x50");
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(invalidUser))
            .exchange()
            .expectStatus()
            .isBadRequest();

        Optional<User> user = userRepository.findOneByEmailIgnoreCase("funky@example.com").blockOptional();
        assertThat(user).isEmpty();
    }

    static Stream<ManagedUserVM> invalidUsers() {
        return Stream.of(
            createInvalidUser("bob", "password", "Bob", "Green", "invalid", true), // <-- invalid
            createInvalidUser("bob", "123", "Bob", "Green", "bob@example.com", true), // password with only 3 digits
            createInvalidUser("bob", null, "Bob", "Green", "bob@example.com", true) // invalid null password
        );
    }

    @ParameterizedTest
    @MethodSource("invalidUsers")
    void testRegisterInvalidUsers(ManagedUserVM invalidUser) throws Exception {
        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(invalidUser))
            .exchange()
            .expectStatus()
            .isBadRequest();

        Optional<User> user = userRepository.findOneByLogin("bob").blockOptional();
        assertThat(user).isEmpty();
    }

    private static ManagedUserVM createInvalidUser(
        String login,
        String password,
        String firstName,
        String lastName,
        String email,
        boolean activated
    ) {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin(login);
        invalidUser.setPassword(password);
        invalidUser.setFirstName(firstName);
        invalidUser.setLastName(lastName);
        invalidUser.setEmail(email);
        invalidUser.setActivated(activated);
        invalidUser.setImageUrl("http://placehold.it/50x50");
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));
        return invalidUser;
    }

    @Test
    void testRegisterDuplicateLogin() throws Exception {
        // First registration
        ManagedUserVM firstUser = new ManagedUserVM();
        firstUser.setLogin("alice");
        firstUser.setPassword("password");
        firstUser.setFirstName("Alice");
        firstUser.setLastName("Something");
        firstUser.setEmail("alice@example.com");
        firstUser.setImageUrl("http://placehold.it/50x50");
        firstUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        firstUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Duplicate login, different email
        ManagedUserVM secondUser = new ManagedUserVM();
        secondUser.setLogin(firstUser.getLogin());
        secondUser.setPassword(firstUser.getPassword());
        secondUser.setFirstName(firstUser.getFirstName());
        secondUser.setLastName(firstUser.getLastName());
        secondUser.setEmail("alice2@example.com");
        secondUser.setImageUrl(firstUser.getImageUrl());
        secondUser.setLangKey(firstUser.getLangKey());
        secondUser.setCreatedBy(firstUser.getCreatedBy());
        secondUser.setCreatedDate(firstUser.getCreatedDate());
        secondUser.setLastModifiedBy(firstUser.getLastModifiedBy());
        secondUser.setLastModifiedDate(firstUser.getLastModifiedDate());
        secondUser.setAuthorities(new HashSet<>(firstUser.getAuthorities()));

        // First user
        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(firstUser))
            .exchange()
            .expectStatus()
            .isCreated();

        // Second (non activated) user
        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(secondUser))
            .exchange()
            .expectStatus()
            .isCreated();

        Optional<User> testUser = userRepository.findOneByEmailIgnoreCase("alice2@example.com").blockOptional();
        assertThat(testUser).isPresent();
        testUser.orElseThrow().setActivated(true);
        userRepository.save(testUser.orElseThrow()).block();

        // Second (already activated) user
        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(secondUser))
            .exchange()
            .expectStatus()
            .isBadRequest();

        userService.deleteUser("alice").block();
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        // First user
        ManagedUserVM firstUser = new ManagedUserVM();
        firstUser.setLogin("test-register-duplicate-email");
        firstUser.setPassword("password");
        firstUser.setFirstName("Alice");
        firstUser.setLastName("Test");
        firstUser.setEmail("test-register-duplicate-email@example.com");
        firstUser.setImageUrl("http://placehold.it/50x50");
        firstUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        firstUser.setAuthorities(Collections.singleton(AuthoritiesConstants.USER));

        // Register first user
        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(firstUser))
            .exchange()
            .expectStatus()
            .isCreated();

        Optional<User> testUser1 = userRepository.findOneByLogin("test-register-duplicate-email").blockOptional();
        assertThat(testUser1).isPresent();

        // Duplicate email, different login
        ManagedUserVM secondUser = new ManagedUserVM();
        secondUser.setLogin("test-register-duplicate-email-2");
        secondUser.setPassword(firstUser.getPassword());
        secondUser.setFirstName(firstUser.getFirstName());
        secondUser.setLastName(firstUser.getLastName());
        secondUser.setEmail(firstUser.getEmail());
        secondUser.setImageUrl(firstUser.getImageUrl());
        secondUser.setLangKey(firstUser.getLangKey());
        secondUser.setAuthorities(new HashSet<>(firstUser.getAuthorities()));

        // Register second (non activated) user
        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(secondUser))
            .exchange()
            .expectStatus()
            .isCreated();

        Optional<User> testUser2 = userRepository.findOneByLogin("test-register-duplicate-email").blockOptional();
        assertThat(testUser2).isEmpty();

        Optional<User> testUser3 = userRepository.findOneByLogin("test-register-duplicate-email-2").blockOptional();
        assertThat(testUser3).isPresent();

        // Duplicate email - with uppercase email address
        ManagedUserVM userWithUpperCaseEmail = new ManagedUserVM();
        userWithUpperCaseEmail.setId(firstUser.getId());
        userWithUpperCaseEmail.setLogin("test-register-duplicate-email-3");
        userWithUpperCaseEmail.setPassword(firstUser.getPassword());
        userWithUpperCaseEmail.setFirstName(firstUser.getFirstName());
        userWithUpperCaseEmail.setLastName(firstUser.getLastName());
        userWithUpperCaseEmail.setEmail("TEST-register-duplicate-email@example.com");
        userWithUpperCaseEmail.setImageUrl(firstUser.getImageUrl());
        userWithUpperCaseEmail.setLangKey(firstUser.getLangKey());
        userWithUpperCaseEmail.setAuthorities(new HashSet<>(firstUser.getAuthorities()));

        // Register third (not activated) user
        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userWithUpperCaseEmail))
            .exchange()
            .expectStatus()
            .isCreated();

        Optional<User> testUser4 = userRepository.findOneByLogin("test-register-duplicate-email-3").blockOptional();
        assertThat(testUser4).isPresent();
        assertThat(testUser4.orElseThrow().getEmail()).isEqualTo("test-register-duplicate-email@example.com");

        testUser4.orElseThrow().setActivated(true);
        userService.updateUser((new AdminUserDTO(testUser4.orElseThrow()))).block();

        // Register 4th (already activated) user
        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(secondUser))
            .exchange()
            .expectStatus()
            .is4xxClientError();

        userService.deleteUser("test-register-duplicate-email-3").block();
    }

    @Test
    void testRegisterAdminIsIgnored() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setLogin("badguy");
        validUser.setPassword("password");
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setEmail("badguy@example.com");
        validUser.setActivated(true);
        validUser.setImageUrl("http://placehold.it/50x50");
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        accountWebTestClient
            .post()
            .uri("/api/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(validUser))
            .exchange()
            .expectStatus()
            .isCreated();

        Optional<User> userDup = userRepository.findOneWithAuthoritiesByLogin("badguy").blockOptional();
        assertThat(userDup).isPresent();
        assertThat(userDup.orElseThrow().getAuthorities())
            .hasSize(1)
            .containsExactly(authorityRepository.findByCode(AuthoritiesConstants.USER).block());

        userService.deleteUser("badguy").block();
    }

    @Test
    void testActivateAccount() {
        final String activationKey = "some activation key";
        User user = new User();
        user.setLogin("activate-account");
        user.setEmail("activate-account@example.com");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(false);
        user.setActivationKey(activationKey);
        user.setCreatedBy(Constants.SYSTEM);

        userRepository.save(user).block();

        accountWebTestClient.get().uri("/api/activate?key={activationKey}", activationKey).exchange().expectStatus().isOk();

        user = userRepository.findOneByLogin(user.getLogin()).block();
        assertThat(user.isActivated()).isTrue();

        userService.deleteUser("activate-account").block();
    }

    @Test
    void testActivateAccountWithWrongKey() {
        accountWebTestClient
            .get()
            .uri("/api/activate?key=wrongActivationKey")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @WithMockUser("save-account")
    void testSaveAccount() throws Exception {
        User user = new User();
        user.setLogin("save-account");
        user.setEmail("save-account@example.com");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-account@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        accountWebTestClient
            .post()
            .uri("/api/account")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userDTO))
            .exchange()
            .expectStatus()
            .isOk();

        User updatedUser = userRepository.findOneWithAuthoritiesByLogin(user.getLogin()).block();
        assertThat(updatedUser.getFirstName()).isEqualTo(userDTO.getFirstName());
        assertThat(updatedUser.getLastName()).isEqualTo(userDTO.getLastName());
        assertThat(updatedUser.getEmail()).isEqualTo(userDTO.getEmail());
        assertThat(updatedUser.getLangKey()).isEqualTo(userDTO.getLangKey());
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
        assertThat(updatedUser.getImageUrl()).isEqualTo(userDTO.getImageUrl());
        assertThat(updatedUser.isActivated()).isTrue();
        assertThat(updatedUser.getAuthorities()).isEmpty();

        userService.deleteUser("save-account").block();
    }

    @Test
    @WithMockUser("save-invalid-email")
    void testSaveInvalidEmail() throws Exception {
        User user = new User();
        user.setLogin("save-invalid-email");
        user.setEmail("save-invalid-email@example.com");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setCreatedBy(Constants.SYSTEM);

        userRepository.save(user).block();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("invalid email");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        accountWebTestClient
            .post()
            .uri("/api/account")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        assertThat(userRepository.findOneByEmailIgnoreCase("invalid email").blockOptional()).isNotPresent();

        userService.deleteUser("save-invalid-email").block();
    }

    @Test
    @WithMockUser("save-existing-email")
    void testSaveExistingEmail() throws Exception {
        User user = new User();
        user.setLogin("save-existing-email");
        user.setEmail("save-existing-email@example.com");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        User anotherUser = new User();
        anotherUser.setLogin("save-existing-email2");
        anotherUser.setEmail("save-existing-email2@example.com");
        anotherUser.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        anotherUser.setActivated(true);
        anotherUser.setCreatedBy(Constants.SYSTEM);

        userRepository.save(anotherUser).block();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-existing-email2@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        accountWebTestClient
            .post()
            .uri("/api/account")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userDTO))
            .exchange()
            .expectStatus()
            .isBadRequest();

        User updatedUser = userRepository.findOneByLogin("save-existing-email").block();
        assertThat(updatedUser.getEmail()).isEqualTo("save-existing-email@example.com");

        userService.deleteUser("save-existing-email").block();
        userService.deleteUser("save-existing-email2").block();
    }

    @Test
    @WithMockUser("save-existing-email-and-login")
    void testSaveExistingEmailAndLogin() throws Exception {
        User user = new User();
        user.setLogin("save-existing-email-and-login");
        user.setEmail("save-existing-email-and-login@example.com");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        AdminUserDTO userDTO = new AdminUserDTO();
        userDTO.setLogin("not-used");
        userDTO.setFirstName("firstname");
        userDTO.setLastName("lastname");
        userDTO.setEmail("save-existing-email-and-login@example.com");
        userDTO.setActivated(false);
        userDTO.setImageUrl("http://placehold.it/50x50");
        userDTO.setLangKey(Constants.DEFAULT_LANGUAGE);
        userDTO.setAuthorities(Collections.singleton(AuthoritiesConstants.ADMIN));

        accountWebTestClient
            .post()
            .uri("/api/account")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(userDTO))
            .exchange()
            .expectStatus()
            .isOk();

        User updatedUser = userRepository.findOneByLogin("save-existing-email-and-login").block();
        assertThat(updatedUser.getEmail()).isEqualTo("save-existing-email-and-login@example.com");

        userService.deleteUser("save-existing-email-and-login").block();
    }

    @Test
    @WithMockUser("change-password-wrong-existing-password")
    void testChangePasswordWrongExistingPassword() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.insecure().nextAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-wrong-existing-password");
        user.setEmail("change-password-wrong-existing-password@example.com");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        accountWebTestClient
            .post()
            .uri("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(new PasswordChangeDTO("1" + currentPassword, "new password")))
            .exchange()
            .expectStatus()
            .isBadRequest();

        User updatedUser = userRepository.findOneByLogin("change-password-wrong-existing-password").block();
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(currentPassword, updatedUser.getPassword())).isTrue();

        userService.deleteUser("change-password-wrong-existing-password").block();
    }

    @Test
    @WithMockUser("change-password")
    void testChangePassword() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.insecure().nextAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password");
        user.setEmail("change-password@example.com");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        accountWebTestClient
            .post()
            .uri("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(new PasswordChangeDTO(currentPassword, "new password")))
            .exchange()
            .expectStatus()
            .isOk();

        User updatedUser = userRepository.findOneByLogin("change-password").block();
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isTrue();

        userService.deleteUser("change-password").block();
    }

    @Test
    @WithMockUser("change-password-too-small")
    void testChangePasswordTooSmall() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.insecure().nextAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-too-small");
        user.setEmail("change-password-too-small@example.com");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        String newPassword = RandomStringUtils.insecure().nextAlphanumeric(ManagedUserVM.PASSWORD_MIN_LENGTH - 1);

        accountWebTestClient
            .post()
            .uri("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(new PasswordChangeDTO(currentPassword, newPassword)))
            .exchange()
            .expectStatus()
            .isBadRequest();

        User updatedUser = userRepository.findOneByLogin("change-password-too-small").block();
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());

        userService.deleteUser("change-password-too-small").block();
    }

    @Test
    @WithMockUser("change-password-too-long")
    void testChangePasswordTooLong() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.insecure().nextAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-too-long");
        user.setEmail("change-password-too-long@example.com");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        String newPassword = RandomStringUtils.insecure().nextAlphanumeric(ManagedUserVM.PASSWORD_MAX_LENGTH + 1);

        accountWebTestClient
            .post()
            .uri("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(new PasswordChangeDTO(currentPassword, newPassword)))
            .exchange()
            .expectStatus()
            .isBadRequest();

        User updatedUser = userRepository.findOneByLogin("change-password-too-long").block();
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());

        userService.deleteUser("change-password-too-long").block();
    }

    @Test
    @WithMockUser("change-password-empty")
    void testChangePasswordEmpty() throws Exception {
        User user = new User();
        String currentPassword = RandomStringUtils.insecure().nextAlphanumeric(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-empty");
        user.setEmail("change-password-empty@example.com");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        accountWebTestClient
            .post()
            .uri("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(new PasswordChangeDTO(currentPassword, "")))
            .exchange()
            .expectStatus()
            .isBadRequest();

        User updatedUser = userRepository.findOneByLogin("change-password-empty").block();
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());

        userService.deleteUser("change-password-empty").block();
    }

    @Test
    void testRequestPasswordReset() {
        User user = new User();
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setLogin("password-reset");
        user.setEmail("password-reset@example.com");
        user.setLangKey("en");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        accountWebTestClient
            .post()
            .uri("/api/account/reset-password/init")
            .bodyValue("password-reset@example.com")
            .exchange()
            .expectStatus()
            .isOk();

        userService.deleteUser("password-reset").block();
    }

    @Test
    void testRequestPasswordResetUpperCaseEmail() {
        User user = new User();
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setLogin("password-reset-upper-case");
        user.setEmail("password-reset-upper-case@example.com");
        user.setLangKey("en");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        accountWebTestClient
            .post()
            .uri("/api/account/reset-password/init")
            .bodyValue("password-reset-upper-case@EXAMPLE.COM")
            .exchange()
            .expectStatus()
            .isOk();

        userService.deleteUser("password-reset-upper-case").block();
    }

    @Test
    void testRequestPasswordResetWrongEmail() {
        accountWebTestClient
            .post()
            .uri("/api/account/reset-password/init")
            .bodyValue("password-reset-wrong-email@example.com")
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void testFinishPasswordReset() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setLogin("finish-password-reset");
        user.setEmail("finish-password-reset@example.com");
        user.setResetDate(Instant.now().plusSeconds(60));
        user.setResetKey("reset key");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("new password");

        accountWebTestClient
            .post()
            .uri("/api/account/reset-password/finish")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(keyAndPassword))
            .exchange()
            .expectStatus()
            .isOk();

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).block();
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isTrue();

        userService.deleteUser("finish-password-reset").block();
    }

    @Test
    void testFinishPasswordResetTooSmall() throws Exception {
        User user = new User();
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setLogin("finish-password-reset-too-small");
        user.setEmail("finish-password-reset-too-small@example.com");
        user.setResetDate(Instant.now().plusSeconds(60));
        user.setResetKey("reset key too small");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("foo");

        accountWebTestClient
            .post()
            .uri("/api/account/reset-password/finish")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(keyAndPassword))
            .exchange()
            .expectStatus()
            .isBadRequest();

        User updatedUser = userRepository.findOneByLogin(user.getLogin()).block();
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isFalse();

        userService.deleteUser("finish-password-reset-too-small").block();
    }

    @Test
    void testFinishPasswordResetWrongKey() throws Exception {
        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey("wrong reset key");
        keyAndPassword.setNewPassword("new password");

        accountWebTestClient
            .post()
            .uri("/api/account/reset-password/finish")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(om.writeValueAsBytes(keyAndPassword))
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @WithMockUser("update-locale-user-es")
    void testUpdateLocaleToSpanish() throws Exception {
        // Create test user
        User user = new User();
        user.setLogin("update-locale-user-es");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail("update-locale-es@example.com");
        user.setFirstName("Update");
        user.setLastName("Locale ES");
        user.setLangKey("en"); // Initially English
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        // Update locale to Spanish
        accountWebTestClient
            .patch()
            .uri("/api/account/locale")
            .contentType(MediaType.TEXT_PLAIN)
            .bodyValue("es")
            .exchange()
            .expectStatus()
            .isOk();

        // Verify locale was updated in database
        User updatedUser = userRepository.findOneByLogin("update-locale-user-es").block();
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getLangKey()).isEqualTo("es");

        userService.deleteUser("update-locale-user-es").block();
    }

    @Test
    @WithMockUser("update-locale-user-en")
    void testUpdateLocaleToEnglish() throws Exception {
        // Create test user
        User user = new User();
        user.setLogin("update-locale-user-en");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail("update-locale-en@example.com");
        user.setFirstName("Update");
        user.setLastName("Locale EN");
        user.setLangKey("es"); // Initially Spanish
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        // Update locale to English
        accountWebTestClient
            .patch()
            .uri("/api/account/locale")
            .contentType(MediaType.TEXT_PLAIN)
            .bodyValue("en")
            .exchange()
            .expectStatus()
            .isOk();

        // Verify locale was updated in database
        User updatedUser = userRepository.findOneByLogin("update-locale-user-en").block();
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getLangKey()).isEqualTo("en");

        userService.deleteUser("update-locale-user-en").block();
    }

    @Test
    @WithMockUser("update-locale-user-invalid")
    void testUpdateLocaleWithInvalidLanguage() throws Exception {
        // Create test user
        User user = new User();
        user.setLogin("update-locale-user-invalid");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail("update-locale-invalid@example.com");
        user.setFirstName("Update");
        user.setLastName("Locale Invalid");
        user.setLangKey("es");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        // Try to update locale with invalid language (French)
        // Pass X-Locale: es to get Spanish error message (highest priority)
        accountWebTestClient
            .patch()
            .uri("/api/account/locale")
            .contentType(MediaType.TEXT_PLAIN)
            .header("X-Locale", "es") // Request error message in Spanish
            .bodyValue("fr")
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody()
            .jsonPath("$.title")
            .isEqualTo("Clave de idioma inválida") // Title translated to Spanish
            .jsonPath("$.detail")
            .value(org.hamcrest.Matchers.containsString("soportan")); // Detail also in Spanish

        // Verify locale was NOT updated in database
        User updatedUser = userRepository.findOneByLogin("update-locale-user-invalid").block();
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getLangKey()).isEqualTo("es"); // Still Spanish

        userService.deleteUser("update-locale-user-invalid").block();
    }

    @Test
    @WithMockUser("update-locale-error-english")
    void testUpdateLocaleWithInvalidLanguageReturnsErrorInEnglish() throws Exception {
        // Create test user
        User user = new User();
        user.setLogin("update-locale-error-english");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail("update-locale-error-english@example.com");
        user.setFirstName("Update");
        user.setLastName("Locale Error English");
        user.setLangKey("en");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        // Try to update locale with invalid language (French), passing X-Locale: en
        accountWebTestClient
            .patch()
            .uri("/api/account/locale")
            .contentType(MediaType.TEXT_PLAIN)
            .header("X-Locale", "en") // Request error message in English
            .bodyValue("fr")
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody()
            .jsonPath("$.title")
            .isEqualTo("Invalid language key") // Title in English
            .jsonPath("$.detail")
            .value(org.hamcrest.Matchers.containsString("Only")); // Detail in English

        // Verify locale was NOT updated in database
        User updatedUser = userRepository.findOneByLogin("update-locale-error-english").block();
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getLangKey()).isEqualTo("en"); // Still English

        userService.deleteUser("update-locale-error-english").block();
    }

    @Test
    void testUpdateLocaleWithoutAuthentication() throws Exception {
        // Try to update locale without authentication
        accountWebTestClient
            .patch()
            .uri("/api/account/locale")
            .contentType(MediaType.TEXT_PLAIN)
            .bodyValue("es")
            .exchange()
            .expectStatus()
            .isUnauthorized();
    }

    @Test
    @WithMockUser("update-locale-persistence")
    void testUpdateLocalePersistence() throws Exception {
        // Create test user
        User user = new User();
        user.setLogin("update-locale-persistence");
        user.setPassword(RandomStringUtils.insecure().nextAlphanumeric(60));
        user.setActivated(true);
        user.setEmail("update-locale-persistence@example.com");
        user.setFirstName("Persistence");
        user.setLastName("Test");
        user.setLangKey("es");
        user.setCreatedBy(Constants.SYSTEM);
        userRepository.save(user).block();

        // Update locale to English
        accountWebTestClient
            .patch()
            .uri("/api/account/locale")
            .contentType(MediaType.TEXT_PLAIN)
            .bodyValue("en")
            .exchange()
            .expectStatus()
            .isOk();

        // Verify change persisted by fetching user again
        User firstFetch = userRepository.findOneByLogin("update-locale-persistence").block();
        assertThat(firstFetch).isNotNull();
        assertThat(firstFetch.getLangKey()).isEqualTo("en");

        // Update back to Spanish
        accountWebTestClient
            .patch()
            .uri("/api/account/locale")
            .contentType(MediaType.TEXT_PLAIN)
            .bodyValue("es")
            .exchange()
            .expectStatus()
            .isOk();

        // Verify second change also persisted
        User secondFetch = userRepository.findOneByLogin("update-locale-persistence").block();
        assertThat(secondFetch).isNotNull();
        assertThat(secondFetch.getLangKey()).isEqualTo("es");

        userService.deleteUser("update-locale-persistence").block();
    }
}
