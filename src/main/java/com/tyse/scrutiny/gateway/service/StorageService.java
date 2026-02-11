package com.tyse.scrutiny.gateway.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Service for generating presigned URLs for MinIO object storage.
 */
@Service
public class StorageService {

    private static final Logger LOG = LoggerFactory.getLogger(StorageService.class);

    private final MinioClient minioClient;
    private final String bucket;
    private final int presignedUrlExpiry;

    public StorageService(
        MinioClient minioClient,
        @Value("${minio.bucket}") String bucket,
        @Value("${minio.presigned-url-expiry}") int presignedUrlExpiry
    ) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        this.presignedUrlExpiry = presignedUrlExpiry;
    }

    /**
     * Generate a presigned URL for uploading a file.
     *
     * @param fileName original file name
     * @param contentType MIME type of the file
     * @return PresignedUploadResult containing the upload URL and object key
     */
    public Mono<PresignedUploadResult> generatePresignedUploadUrl(String fileName, String contentType) {
        return Mono.fromCallable(() -> {
            String objectKey = generateObjectKey(fileName);
            LOG.debug("Generating presigned upload URL for object: {}", objectKey);

            String uploadUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(presignedUrlExpiry, TimeUnit.SECONDS)
                    .build()
            );

            LOG.info("Generated presigned upload URL for object: {}", objectKey);
            return new PresignedUploadResult(uploadUrl, objectKey, bucket);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Generate a presigned URL for downloading a file.
     *
     * @param objectKey the object key in the bucket
     * @return presigned download URL
     */
    public Mono<String> generatePresignedDownloadUrl(String objectKey) {
        return Mono.fromCallable(() -> {
            LOG.debug("Generating presigned download URL for object: {}", objectKey);

            String downloadUrl = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(presignedUrlExpiry, TimeUnit.SECONDS)
                    .build()
            );

            LOG.info("Generated presigned download URL for object: {}", objectKey);
            return downloadUrl;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Generate a unique object key with date-based path organization.
     * Format: yyyy/MM/uuid-filename.ext
     */
    private String generateObjectKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String sanitizedFileName = sanitizeFileName(fileName);
        return String.format("%s/%s-%s", datePath, uniqueId, sanitizedFileName);
    }

    /**
     * Sanitize file name to remove potentially problematic characters.
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Result object for presigned upload URL generation.
     */
    public record PresignedUploadResult(String uploadUrl, String objectKey, String bucket) {}
}
