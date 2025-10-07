package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.service.RecaptchaService;
import com.tyse.scrutiny.gateway.service.dto.RecaptchaRequestDTO;
import com.tyse.scrutiny.gateway.service.dto.RecaptchaResponseDTO;
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
public class RecaptchaController {

    private static final Logger LOG = LoggerFactory.getLogger(RecaptchaController.class);

    private final RecaptchaService recaptchaService;

    public RecaptchaController(RecaptchaService recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    @PostMapping("/verify")
    public ResponseEntity<RecaptchaResponseDTO> verifyRecaptcha(@Valid @RequestBody RecaptchaRequestDTO recaptchaRequestDTO) {
        LOG.debug("REST request to verify reCaptcha for action: {}", recaptchaRequestDTO.getAction());
        boolean isValid = recaptchaService.verifyRecaptcha(recaptchaRequestDTO.getToken(), recaptchaRequestDTO.getAction());
        RecaptchaResponseDTO responseDTO = new RecaptchaResponseDTO();
        responseDTO.setSuccess(isValid);
        return ResponseEntity.ok(responseDTO);
    }
}
