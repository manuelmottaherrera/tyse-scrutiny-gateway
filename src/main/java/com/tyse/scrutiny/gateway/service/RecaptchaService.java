package com.tyse.scrutiny.gateway.service;

import com.tyse.scrutiny.gateway.service.dto.RecaptchaAPIResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {

    @Value("${google.recaptcha.site-key}")
    private String secretKey;

    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    private RestTemplate restTemplate;

    public RecaptchaService() {
        this.restTemplate = new RestTemplate();
    }

    public boolean verifyRecaptcha(String token, String action) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", secretKey);
        params.add("response", token);

        try {
            RecaptchaAPIResponseDTO response = restTemplate.postForObject(RECAPTCHA_VERIFY_URL, params, RecaptchaAPIResponseDTO.class);
            return response != null && response.isSuccess() && response.getScore() >= 0.5 && action.equals(response.getAction());
        } catch (Exception e) {
            return false;
        }
    }
}
