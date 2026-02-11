package com.tyse.scrutiny.gateway.web.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.tyse.scrutiny.gateway.service.StorageService;
import com.tyse.scrutiny.gateway.service.StorageService.PresignedUploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class StorageResourceTest {

    @Mock
    private StorageService storageService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        StorageResource storageResource = new StorageResource(storageService);
        webTestClient = WebTestClient.bindToController(storageResource).build();
    }

    @Test
    void generatePresignedUploadUrl_shouldReturnUploadUrl() {
        // Given
        PresignedUploadResult mockResult = new PresignedUploadResult(
            "http://localhost:9000/e14-pdfs/2026/02/abc123-test.pdf?signature=xyz",
            "2026/02/abc123-test.pdf",
            "e14-pdfs"
        );

        when(storageService.generatePresignedUploadUrl(anyString(), anyString())).thenReturn(Mono.just(mockResult));

        // When/Then
        webTestClient
            .post()
            .uri("/api/storage/presigned-url")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"fileName\": \"test.pdf\", \"contentType\": \"application/pdf\"}")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.uploadUrl")
            .isEqualTo(mockResult.uploadUrl())
            .jsonPath("$.objectKey")
            .isEqualTo(mockResult.objectKey())
            .jsonPath("$.bucket")
            .isEqualTo(mockResult.bucket())
            .jsonPath("$.error")
            .isEmpty();
    }

    @Test
    void generatePresignedUploadUrl_withInvalidContentType_shouldReturnBadRequest() {
        // When/Then - contentType is not PDF
        webTestClient
            .post()
            .uri("/api/storage/presigned-url")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"fileName\": \"test.txt\", \"contentType\": \"text/plain\"}")
            .exchange()
            .expectStatus()
            .isBadRequest()
            .expectBody()
            .jsonPath("$.error")
            .isEqualTo("Only PDF files are allowed");
    }

    @Test
    void generatePresignedUploadUrl_withMissingFileName_shouldReturnBadRequest() {
        // When/Then
        webTestClient
            .post()
            .uri("/api/storage/presigned-url")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"contentType\": \"application/pdf\"}")
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    void generatePresignedUploadUrl_withMissingContentType_shouldReturnBadRequest() {
        // When/Then
        webTestClient
            .post()
            .uri("/api/storage/presigned-url")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"fileName\": \"test.pdf\"}")
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    void generatePresignedDownloadUrl_shouldReturnDownloadUrl() {
        // Given
        String objectKey = "2026/02/abc123-test.pdf";
        String downloadUrl = "http://localhost:9000/e14-pdfs/2026/02/abc123-test.pdf?signature=xyz";

        when(storageService.generatePresignedDownloadUrl(objectKey)).thenReturn(Mono.just(downloadUrl));

        // When/Then
        webTestClient
            .get()
            .uri(uriBuilder -> uriBuilder.path("/api/storage/presigned-url").queryParam("objectKey", objectKey).build())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.downloadUrl")
            .isEqualTo(downloadUrl)
            .jsonPath("$.objectKey")
            .isEqualTo(objectKey);
    }

    @Test
    void generatePresignedUploadUrl_withInvalidFileName_shouldReturnBadRequest() {
        // When/Then - fileName has invalid characters
        webTestClient
            .post()
            .uri("/api/storage/presigned-url")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"fileName\": \"test file (1).pdf\", \"contentType\": \"application/pdf\"}")
            .exchange()
            .expectStatus()
            .isBadRequest();
    }

    @Test
    void generatePresignedUploadUrl_withValidFileName_shouldSucceed() {
        // Given
        PresignedUploadResult mockResult = new PresignedUploadResult("http://localhost:9000/url", "2026/02/abc-test_e14.pdf", "e14-pdfs");

        when(storageService.generatePresignedUploadUrl(anyString(), anyString())).thenReturn(Mono.just(mockResult));

        // When/Then - valid fileName with allowed characters
        webTestClient
            .post()
            .uri("/api/storage/presigned-url")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"fileName\": \"test_e14-v2.pdf\", \"contentType\": \"application/pdf\"}")
            .exchange()
            .expectStatus()
            .isOk();
    }
}
