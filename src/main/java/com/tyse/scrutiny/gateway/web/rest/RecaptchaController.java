package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.service.RecaptchaService;
import com.tyse.scrutiny.gateway.service.dto.RecaptchaRequestDTO;
import com.tyse.scrutiny.gateway.service.dto.RecaptchaResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing reCaptcha verification.
 */
@RestController
@RequestMapping("/api/recaptcha")
@Tag(name = "ReCAPTCHA", description = "API para verificación de Google reCAPTCHA v3")
public class RecaptchaController {

    private static final Logger LOG = LoggerFactory.getLogger(RecaptchaController.class);

    private final RecaptchaService recaptchaService;

    public RecaptchaController(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    @Operation(
        summary = "Verificar token de reCAPTCHA",
        description = "Valida un token de Google reCAPTCHA v3 generado en el cliente contra la API de Google para prevenir bots y spam"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Verificación completada - revisar campo 'success' en la respuesta",
                content = @Content(schema = @Schema(implementation = RecaptchaResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content),
        }
    )
    @PostMapping("/verify")
    public ResponseEntity<RecaptchaResponseDTO> verifyRecaptcha(
        @Parameter(
            description = "Token de reCAPTCHA y acción a verificar",
            required = true
        ) @Valid @RequestBody RecaptchaRequestDTO recaptchaRequestDTO
    ) {
        LOG.debug("REST request to verify reCaptcha for action: {}", recaptchaRequestDTO.getAction());
        boolean isValid = recaptchaService.verifyRecaptcha(recaptchaRequestDTO.getToken(), recaptchaRequestDTO.getAction());
        RecaptchaResponseDTO responseDTO = new RecaptchaResponseDTO();
        responseDTO.setSuccess(isValid);
        return ResponseEntity.ok(responseDTO);
    }
}
