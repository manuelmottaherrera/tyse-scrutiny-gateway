package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.service.StorageService;
import com.tyse.scrutiny.gateway.service.StorageService.PresignedUploadResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for managing storage operations.
 * Provides endpoints for generating presigned URLs for direct uploads to MinIO.
 */
@RestController
@RequestMapping("/api/storage")
public class StorageResource {

    private static final Logger LOG = LoggerFactory.getLogger(StorageResource.class);

    private final StorageService storageService;

    public StorageResource(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * POST /api/storage/presigned-url : Generate a presigned URL for file upload.
     *
     * @param request the upload request containing fileName and contentType
     * @return the presigned upload URL and object key
     */
    @PostMapping("/presigned-url")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<PresignedUrlResponse>> generatePresignedUploadUrl(@Valid @RequestBody PresignedUrlRequest request) {
        LOG.debug("REST request to generate presigned upload URL for file: {}", request.fileName());

        if (!isValidContentType(request.contentType())) {
            return Mono.just(ResponseEntity.badRequest().body(new PresignedUrlResponse(null, null, null, "Only PDF files are allowed")));
        }

        return storageService
            .generatePresignedUploadUrl(request.fileName(), request.contentType())
            .map(result -> ResponseEntity.ok(new PresignedUrlResponse(result.uploadUrl(), result.objectKey(), result.bucket(), null)));
    }

    /**
     * GET /api/storage/presigned-url/{objectKey} : Generate a presigned URL for file download.
     *
     * @param objectKey the object key in the bucket (URL encoded)
     * @return the presigned download URL
     */
    @GetMapping("/presigned-url")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<PresignedDownloadResponse>> generatePresignedDownloadUrl(@RequestParam String objectKey) {
        LOG.debug("REST request to generate presigned download URL for object: {}", objectKey);

        return storageService
            .generatePresignedDownloadUrl(objectKey)
            .map(downloadUrl -> ResponseEntity.ok(new PresignedDownloadResponse(downloadUrl, objectKey)));
    }

    private boolean isValidContentType(String contentType) {
        return contentType != null && contentType.equalsIgnoreCase("application/pdf");
    }

    /**
     * Request DTO for presigned upload URL generation.
     */
    public record PresignedUrlRequest(
        @NotBlank(message = "fileName is required") @Pattern(
            regexp = "^[a-zA-Z0-9._-]+$",
            message = "fileName contains invalid characters"
        ) String fileName,
        @NotBlank(message = "contentType is required") String contentType
    ) {}

    /**
     * Response DTO for presigned upload URL.
     */
    public record PresignedUrlResponse(String uploadUrl, String objectKey, String bucket, String error) {}

    /**
     * Response DTO for presigned download URL.
     */
    public record PresignedDownloadResponse(String downloadUrl, String objectKey) {}
}
