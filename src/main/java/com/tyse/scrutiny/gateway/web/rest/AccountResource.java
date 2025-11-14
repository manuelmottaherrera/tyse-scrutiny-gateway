package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.repository.UserRepository;
import com.tyse.scrutiny.gateway.security.SecurityUtils;
import com.tyse.scrutiny.gateway.service.MailService;
import com.tyse.scrutiny.gateway.service.UserService;
import com.tyse.scrutiny.gateway.service.dto.AdminUserDTO;
import com.tyse.scrutiny.gateway.service.dto.PasswordChangeDTO;
import com.tyse.scrutiny.gateway.web.rest.errors.*;
import com.tyse.scrutiny.gateway.web.rest.vm.KeyAndPasswordVM;
import com.tyse.scrutiny.gateway.web.rest.vm.ManagedUserVM;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Account", description = "API para gestionar la cuenta del usuario actual")
public class AccountResource {

    private static class AccountResourceException extends RuntimeException {

        private AccountResourceException(String message) {
            super(message);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AccountResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;

    public AccountResource(UserRepository userRepository, UserService userService, MailService mailService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.mailService = mailService;
    }

    /**
     * {@code POST  /register} : register the user.
     *
     * @param managedUserVM the managed user View Model.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already used.
     */
    @Operation(summary = "Registrar nuevo usuario", description = "Registra un nuevo usuario en el sistema y envía un correo de activación")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Contraseña inválida, email o login ya en uso", content = @Content),
        }
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<Void>> registerAccount(
        @Parameter(description = "Información del usuario a registrar", required = true) @Valid @RequestBody ManagedUserVM managedUserVM
    ) {
        if (isPasswordLengthInvalid(managedUserVM.getPassword())) {
            throw new InvalidPasswordException();
        }
        return userService
            .registerUser(managedUserVM, managedUserVM.getPassword())
            .doOnSuccess(mailService::sendActivationEmail)
            .then(
                Mono.just(
                    ResponseEntity.status(HttpStatus.CREATED)
                        .headers(HeaderUtil.createAlert(applicationName, "register.messages.success", managedUserVM.getLogin()))
                        .build()
                )
            );
    }

    /**
     * {@code GET  /activate} : activate the registered user.
     *
     * @param key the activation key.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be activated.
     */
    @Operation(
        summary = "Activar cuenta de usuario",
        description = "Activa la cuenta de un usuario registrado usando la clave de activación enviada por correo"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Cuenta activada exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error al activar la cuenta - clave inválida o expirada", content = @Content),
        }
    )
    @GetMapping("/activate")
    public Mono<Void> activateAccount(
        @Parameter(description = "Clave de activación recibida por correo", required = true) @RequestParam(value = "key") String key
    ) {
        return userService
            .activateRegistration(key)
            .switchIfEmpty(Mono.error(new AccountResourceException("No user was found for this activation key")))
            .then();
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @return the current user.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */
    @Operation(summary = "Obtener información del usuario actual", description = "Retorna la información completa del usuario autenticado")
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Información del usuario obtenida exitosamente",
                content = @Content(schema = @Schema(implementation = AdminUserDTO.class))
            ),
            @ApiResponse(responseCode = "500", description = "Error al obtener el usuario", content = @Content),
        }
    )
    @GetMapping("/account")
    public Mono<AdminUserDTO> getAccount() {
        return userService
            .getUserWithAuthorities()
            .map(AdminUserDTO::new)
            .switchIfEmpty(Mono.error(new AccountResourceException("User could not be found")));
    }

    /**
     * {@code POST  /account} : update the current user information.
     *
     * @param userDTO the current user information.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user login wasn't found.
     */
    @Operation(
        summary = "Actualizar información del usuario actual",
        description = "Actualiza los datos del perfil del usuario autenticado (nombre, apellido, email, idioma, imagen)"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Email ya está en uso", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error al actualizar - usuario no encontrado", content = @Content),
        }
    )
    @PostMapping("/account")
    public Mono<Void> saveAccount(
        @Parameter(description = "Información actualizada del usuario", required = true) @Valid @RequestBody AdminUserDTO userDTO
    ) {
        return SecurityUtils.getCurrentUserLogin()
            .switchIfEmpty(Mono.error(new AccountResourceException("Current user login not found")))
            .flatMap(userLogin ->
                userRepository
                    .findOneByEmailIgnoreCase(userDTO.getEmail())
                    .filter(existingUser -> !existingUser.getLogin().equalsIgnoreCase(userLogin))
                    .hasElement()
                    .flatMap(emailExists -> {
                        if (emailExists) {
                            throw new EmailAlreadyUsedException();
                        }
                        return userRepository.findOneByLogin(userLogin);
                    })
            )
            .switchIfEmpty(Mono.error(new AccountResourceException("User could not be found")))
            .flatMap(user ->
                userService.updateUser(
                    userDTO.getFirstName(),
                    userDTO.getLastName(),
                    userDTO.getEmail(),
                    userDTO.getLangKey(),
                    userDTO.getImageUrl()
                )
            );
    }

    /**
     * {@code PATCH  /account/locale} : update the language preference of the current user.
     *
     * <p>This endpoint allows the user to change their preferred language (langKey) which will be used
     * for email notifications and other i18n features. The langKey is persisted in the database.
     *
     * @param langKey the language key (e.g., "es" or "en").
     * @return a {@link Mono} emitting the response.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user login wasn't found.
     */
    @Operation(
        summary = "Actualizar preferencia de idioma del usuario",
        description = "Actualiza el idioma preferido (langKey) del usuario autenticado. Se usa para emails y notificaciones."
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Idioma actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Idioma inválido (solo 'es' o 'en' son soportados)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Error al actualizar - usuario no encontrado", content = @Content),
        }
    )
    @PatchMapping("/account/locale")
    public Mono<Void> updateLocale(
        @Parameter(description = "Código de idioma ('es' o 'en')", required = true) @RequestBody String langKey
    ) {
        // Validate langKey (only "es" or "en" are supported)
        if (!langKey.equals("es") && !langKey.equals("en")) {
            throw new InvalidLocaleException();
        }

        return SecurityUtils.getCurrentUserLogin()
            .switchIfEmpty(Mono.error(new AccountResourceException("Current user login not found")))
            .flatMap(userRepository::findOneByLogin)
            .switchIfEmpty(Mono.error(new AccountResourceException("User could not be found")))
            .flatMap(user ->
                userService.updateUser(user.getFirstName(), user.getLastName(), user.getEmail(), langKey, user.getImageUrl())
            );
    }

    /**
     * {@code POST  /account/change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @Operation(
        summary = "Cambiar contraseña del usuario actual",
        description = "Permite al usuario autenticado cambiar su contraseña proporcionando la contraseña actual y la nueva"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Contraseña cambiada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Nueva contraseña inválida (longitud incorrecta)", content = @Content),
        }
    )
    @PostMapping(path = "/account/change-password")
    public Mono<Void> changePassword(
        @Parameter(description = "Contraseña actual y nueva contraseña", required = true) @RequestBody PasswordChangeDTO passwordChangeDto
    ) {
        if (isPasswordLengthInvalid(passwordChangeDto.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        return userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user.
     */
    @Operation(
        summary = "Iniciar proceso de restablecimiento de contraseña",
        description = "Envía un correo electrónico con un enlace para restablecer la contraseña del usuario"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Correo enviado exitosamente (o email no existe, por seguridad no se informa)"
            ),
        }
    )
    @PostMapping(path = "/account/reset-password/init")
    public Mono<Void> requestPasswordReset(
        @Parameter(description = "Correo electrónico del usuario", required = true) @RequestBody String mail
    ) {
        return userService
            .requestPasswordReset(mail)
            .doOnSuccess(user -> {
                if (Objects.nonNull(user)) {
                    mailService.sendPasswordResetMail(user);
                } else {
                    // Pretend the request has been successful to prevent checking which emails really exist
                    // but log that an invalid attempt has been made
                    LOG.warn("Password reset requested for non existing mail");
                }
            })
            .then();
    }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the password could not be reset.
     */
    @Operation(
        summary = "Finalizar restablecimiento de contraseña",
        description = "Completa el proceso de restablecimiento de contraseña usando la clave recibida por correo y la nueva contraseña"
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Contraseña inválida", content = @Content),
            @ApiResponse(responseCode = "500", description = "Clave de restablecimiento inválida o expirada", content = @Content),
        }
    )
    @PostMapping(path = "/account/reset-password/finish")
    public Mono<Void> finishPasswordReset(
        @Parameter(
            description = "Clave de restablecimiento y nueva contraseña",
            required = true
        ) @RequestBody KeyAndPasswordVM keyAndPassword
    ) {
        if (isPasswordLengthInvalid(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        return userService
            .completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey())
            .switchIfEmpty(Mono.error(new AccountResourceException("No user was found for this reset key")))
            .then();
    }

    private static boolean isPasswordLengthInvalid(String password) {
        return (
            StringUtils.isEmpty(password) ||
            password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
            password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH
        );
    }
}
