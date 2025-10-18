package com.tyse.scrutiny.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tyse.scrutiny.gateway.service.dto.RecaptchaAPIResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class RecaptchaServiceTest {

    private RecaptchaService recaptchaService;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        recaptchaService = new RecaptchaService();
        ReflectionTestUtils.setField(recaptchaService, "secretKey", "test-secret-key");
    }

    @Test
    void shouldReturnTrueWhenRecaptchaIsValid() {
        // Given
        String token = "valid-token";
        String action = "login";
        double validScore = 0.8;

        RecaptchaAPIResponseDTO mockResponse = new RecaptchaAPIResponseDTO();
        mockResponse.setSuccess(true);
        mockResponse.setScore(validScore);
        mockResponse.setAction(action);

        // When
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(RecaptchaAPIResponseDTO.class))).thenReturn(
            mockResponse
        );
        ReflectionTestUtils.setField(recaptchaService, "restTemplate", mockRestTemplate);

        boolean result = recaptchaService.verifyRecaptcha(token, action);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenScoreIsBelowThreshold() {
        // Given
        String token = "valid-token";
        String action = "login";
        double lowScore = 0.3;

        RecaptchaAPIResponseDTO mockResponse = new RecaptchaAPIResponseDTO();
        mockResponse.setSuccess(true);
        mockResponse.setScore(lowScore);
        mockResponse.setAction(action);

        // When
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(RecaptchaAPIResponseDTO.class))).thenReturn(
            mockResponse
        );
        ReflectionTestUtils.setField(recaptchaService, "restTemplate", mockRestTemplate);

        boolean result = recaptchaService.verifyRecaptcha(token, action);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenActionDoesNotMatch() {
        // Given
        String token = "valid-token";
        String expectedAction = "login";
        String differentAction = "register";
        double validScore = 0.8;

        RecaptchaAPIResponseDTO mockResponse = new RecaptchaAPIResponseDTO();
        mockResponse.setSuccess(true);
        mockResponse.setScore(validScore);
        mockResponse.setAction(differentAction);

        // When
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(RecaptchaAPIResponseDTO.class))).thenReturn(
            mockResponse
        );
        ReflectionTestUtils.setField(recaptchaService, "restTemplate", mockRestTemplate);

        boolean result = recaptchaService.verifyRecaptcha(token, expectedAction);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenResponseIsNull() {
        // Given
        String token = "valid-token";
        String action = "login";

        // When
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(RecaptchaAPIResponseDTO.class))).thenReturn(null);
        ReflectionTestUtils.setField(recaptchaService, "restTemplate", mockRestTemplate);

        boolean result = recaptchaService.verifyRecaptcha(token, action);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenResponseIsNotSuccess() {
        // Given
        String token = "invalid-token";
        String action = "login";

        RecaptchaAPIResponseDTO mockResponse = new RecaptchaAPIResponseDTO();
        mockResponse.setSuccess(false);
        mockResponse.setScore(0.8);
        mockResponse.setAction(action);

        // When
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(RecaptchaAPIResponseDTO.class))).thenReturn(
            mockResponse
        );
        ReflectionTestUtils.setField(recaptchaService, "restTemplate", mockRestTemplate);

        boolean result = recaptchaService.verifyRecaptcha(token, action);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalseWhenExceptionOccurs() {
        // Given
        String token = "valid-token";
        String action = "login";

        // When
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(RecaptchaAPIResponseDTO.class))).thenThrow(
            new RestClientException("Connection error")
        );
        ReflectionTestUtils.setField(recaptchaService, "restTemplate", mockRestTemplate);

        boolean result = recaptchaService.verifyRecaptcha(token, action);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueWhenScoreIsExactlyThreshold() {
        // Given
        String token = "valid-token";
        String action = "login";
        double thresholdScore = 0.5;

        RecaptchaAPIResponseDTO mockResponse = new RecaptchaAPIResponseDTO();
        mockResponse.setSuccess(true);
        mockResponse.setScore(thresholdScore);
        mockResponse.setAction(action);

        // When
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        when(mockRestTemplate.postForObject(anyString(), any(MultiValueMap.class), eq(RecaptchaAPIResponseDTO.class))).thenReturn(
            mockResponse
        );
        ReflectionTestUtils.setField(recaptchaService, "restTemplate", mockRestTemplate);

        boolean result = recaptchaService.verifyRecaptcha(token, action);

        // Then
        assertThat(result).isTrue();
    }
}
