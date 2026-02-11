package com.tyse.scrutiny.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private MinioClient minioClient;

    private StorageService storageService;

    private static final String BUCKET = "e14-pdfs";
    private static final int EXPIRY = 3600;

    @BeforeEach
    void setUp() {
        storageService = new StorageService(minioClient, BUCKET, EXPIRY);
    }

    @Test
    void generatePresignedUploadUrl_shouldReturnValidResult() throws Exception {
        // Given
        String fileName = "test_e14.pdf";
        String contentType = "application/pdf";
        String expectedUrl = "http://localhost:9000/e14-pdfs/2026/02/abc123-test_e14.pdf?signature=xyz";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(expectedUrl);

        // When
        StorageService.PresignedUploadResult result = storageService.generatePresignedUploadUrl(fileName, contentType).block();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.uploadUrl()).isEqualTo(expectedUrl);
        assertThat(result.bucket()).isEqualTo(BUCKET);
        assertThat(result.objectKey()).contains("test_e14.pdf");
        assertThat(result.objectKey()).matches("\\d{4}/\\d{2}/[a-f0-9]{8}-test_e14\\.pdf");
    }

    @Test
    void generatePresignedUploadUrl_shouldSanitizeFileName() throws Exception {
        // Given
        String fileName = "test file (1).pdf";
        String contentType = "application/pdf";
        String expectedUrl = "http://localhost:9000/e14-pdfs/sanitized.pdf";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(expectedUrl);

        // When
        StorageService.PresignedUploadResult result = storageService.generatePresignedUploadUrl(fileName, contentType).block();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.objectKey()).doesNotContain(" ");
        assertThat(result.objectKey()).doesNotContain("(");
        assertThat(result.objectKey()).doesNotContain(")");
    }

    @Test
    void generatePresignedDownloadUrl_shouldReturnValidUrl() throws Exception {
        // Given
        String objectKey = "2026/02/abc123-test_e14.pdf";
        String expectedUrl = "http://localhost:9000/e14-pdfs/2026/02/abc123-test_e14.pdf?signature=xyz";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(expectedUrl);

        // When
        String downloadUrl = storageService.generatePresignedDownloadUrl(objectKey).block();

        // Then
        assertThat(downloadUrl).isEqualTo(expectedUrl);
    }

    @Test
    void generatePresignedUploadUrl_shouldGenerateUniqueObjectKeys() throws Exception {
        // Given
        String fileName = "test.pdf";
        String contentType = "application/pdf";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn("http://localhost:9000/bucket/object");

        // When
        StorageService.PresignedUploadResult result1 = storageService.generatePresignedUploadUrl(fileName, contentType).block();
        StorageService.PresignedUploadResult result2 = storageService.generatePresignedUploadUrl(fileName, contentType).block();

        // Then
        assertThat(result1.objectKey()).isNotEqualTo(result2.objectKey());
    }

    @Test
    void generatePresignedUploadUrl_shouldIncludeDatePath() throws Exception {
        // Given
        String fileName = "test.pdf";
        String contentType = "application/pdf";

        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn("http://localhost:9000/bucket/object");

        // When
        StorageService.PresignedUploadResult result = storageService.generatePresignedUploadUrl(fileName, contentType).block();

        // Then - objectKey should have format: yyyy/MM/uuid-filename
        assertThat(result.objectKey()).matches("\\d{4}/\\d{2}/[a-f0-9]{8}-test\\.pdf");
    }
}
