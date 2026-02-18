package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.broker.NotificationProducer;
import com.tyse.scrutiny.gateway.config.Constants;
import com.tyse.scrutiny.gateway.domain.User;
import com.tyse.scrutiny.gateway.repository.UserRepository;
import com.tyse.scrutiny.gateway.security.AuthoritiesConstants;
import com.tyse.scrutiny.gateway.service.UserService;
import com.tyse.scrutiny.gateway.service.dto.AdminUserDTO;
import com.tyse.scrutiny.gateway.web.rest.errors.BadRequestAlertException;
import com.tyse.scrutiny.gateway.web.rest.errors.EmailAlreadyUsedException;
import com.tyse.scrutiny.gateway.web.rest.errors.LoginAlreadyUsedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;

/**
 * REST controller for managing users.
 * <p>
 * This class accesses the {@link com.tyse.scrutiny.gateway.domain.User} entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "User Management", description = "API de administración de usuarios - Solo accesible para administradores")
public class UserResource {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
        Arrays.asList(
            "id",
            "login",
            "firstName",
            "lastName",
            "email",
            "activated",
            "langKey",
            "createdBy",
            "createdDate",
            "lastModifiedBy",
            "lastModifiedDate"
        )
    );

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserService userService;

    private final UserRepository userRepository;

    private final NotificationProducer notificationProducer;

    public UserResource(UserService userService, UserRepository userRepository, NotificationProducer notificationProducer) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.notificationProducer = notificationProducer;
    }

    /**
     * {@code POST  /admin/users}  : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends a
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new user, or with status {@code 400 (Bad Request)} if the login or email is already in use.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the login or email is already in use.
     */
    @Operation(
        summary = "Crear nuevo usuario",
        description = "Crea un nuevo usuario en el sistema y envía un correo de activación. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Usuario creado exitosamente",
                content = @Content(schema = @Schema(implementation = User.class))
            ),
            @ApiResponse(responseCode = "400", description = "Login o email ya en uso", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @PostMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<ResponseEntity<User>> createUser(
        @Parameter(description = "Información del usuario a crear", required = true) @Valid @RequestBody AdminUserDTO userDTO
    ) {
        LOG.debug("REST request to save User : {}", userDTO);

        if (userDTO.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists");
            // Lowercase the user login before comparing with database
        }
        return userRepository
            .findOneByLogin(userDTO.getLogin().toLowerCase())
            .hasElement()
            .flatMap(loginExists -> {
                if (Boolean.TRUE.equals(loginExists)) {
                    return Mono.error(new LoginAlreadyUsedException());
                }
                return userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
            })
            .hasElement()
            .flatMap(emailExists -> {
                if (Boolean.TRUE.equals(emailExists)) {
                    return Mono.error(new EmailAlreadyUsedException());
                }
                return userService.createUser(userDTO);
            })
            .delayUntil(notificationProducer::sendCreationEmail)
            .map(user -> {
                try {
                    return ResponseEntity.created(new URI("/api/admin/users/" + user.getLogin()))
                        .headers(HeaderUtil.createAlert(applicationName, "userManagement.created", user.getLogin()))
                        .body(user);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT /admin/users} : Updates an existing User.
     *
     * @param userDTO the user to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already in use.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already in use.
     */
    @Operation(
        summary = "Actualizar usuario existente",
        description = "Actualiza la información de un usuario existente. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Usuario actualizado exitosamente",
                content = @Content(schema = @Schema(implementation = AdminUserDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Email o login ya en uso", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        }
    )
    @PutMapping({ "/users", "/users/{login}" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<ResponseEntity<AdminUserDTO>> updateUser(
        @Parameter(description = "Login del usuario (opcional si está en el body)", required = false) @PathVariable(
            name = "login",
            required = false
        ) @Pattern(regexp = Constants.LOGIN_REGEX) String login,
        @Parameter(description = "Información actualizada del usuario", required = true) @Valid @RequestBody AdminUserDTO userDTO
    ) {
        LOG.debug("REST request to update User : {}", userDTO);
        return userRepository
            .findOneByEmailIgnoreCase(userDTO.getEmail())
            .filter(user -> !user.getId().equals(userDTO.getId()))
            .hasElement()
            .flatMap(emailExists -> {
                if (Boolean.TRUE.equals(emailExists)) {
                    return Mono.error(new EmailAlreadyUsedException());
                }
                return userRepository.findOneByLogin(userDTO.getLogin().toLowerCase());
            })
            .filter(user -> !user.getId().equals(userDTO.getId()))
            .hasElement()
            .flatMap(loginExists -> {
                if (Boolean.TRUE.equals(loginExists)) {
                    return Mono.error(new LoginAlreadyUsedException());
                }
                return userService.updateUser(userDTO);
            })
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
            .map(user ->
                ResponseEntity.ok()
                    .headers(HeaderUtil.createAlert(applicationName, "userManagement.updated", userDTO.getLogin()))
                    .body(user)
            );
    }

    /**
     * {@code GET /admin/users} : get all users with all the details - calling this are only allowed for the administrators.
     *
     * @param request a {@link ServerHttpRequest} request.
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @Operation(
        summary = "Obtener todos los usuarios",
        description = "Retorna una lista paginada de todos los usuarios con sus detalles completos. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<ResponseEntity<Flux<AdminUserDTO>>> getAllUsers(
        @Parameter(hidden = true) @org.springdoc.core.annotations.ParameterObject ServerHttpRequest request,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return userService
            .countManagedUsers()
            .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
            .map(page ->
                PaginationUtil.generatePaginationHttpHeaders(
                    ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                    page
                )
            )
            .map(headers -> ResponseEntity.ok().headers(headers).body(userService.getAllManagedUsers(pageable)));
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }

    /**
     * {@code GET /admin/users/:login} : get the "login" user.
     *
     * @param login the login of the user to find.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "login" user, or with status {@code 404 (Not Found)}.
     */
    @Operation(
        summary = "Obtener usuario por login",
        description = "Retorna la información completa de un usuario específico. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Usuario encontrado",
                content = @Content(schema = @Schema(implementation = AdminUserDTO.class))
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        }
    )
    @GetMapping("/users/{login}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<AdminUserDTO> getUser(
        @Parameter(description = "Login del usuario a buscar", required = true) @PathVariable("login") String login
    ) {
        LOG.debug("REST request to get User : {}", login);
        return userService
            .getUserWithAuthoritiesByLogin(login)
            .map(AdminUserDTO::new)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    /**
     * {@code DELETE /admin/users/:login} : delete the "login" User.
     *
     * @param login the login of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(
        summary = "Eliminar usuario",
        description = "Elimina un usuario del sistema. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado", content = @Content),
        }
    )
    @DeleteMapping("/users/{login}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<ResponseEntity<Void>> deleteUser(
        @Parameter(description = "Login del usuario a eliminar", required = true) @PathVariable("login") @Pattern(
            regexp = Constants.LOGIN_REGEX
        ) String login
    ) {
        LOG.debug("REST request to delete User: {}", login);
        return userService
            .deleteUser(login)
            .then(
                Mono.just(
                    ResponseEntity.noContent().headers(HeaderUtil.createAlert(applicationName, "userManagement.deleted", login)).build()
                )
            );
    }
}
