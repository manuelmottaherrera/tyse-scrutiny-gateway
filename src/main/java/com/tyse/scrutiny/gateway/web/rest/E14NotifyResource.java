package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.broker.E14UploadProducer;
import com.tyse.scrutiny.gateway.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for E14 PDF upload notifications.
 * After a PDF is uploaded to MinIO, the frontend calls this endpoint
 * to notify the system and trigger the OCR pipeline.
 */
@RestController
@RequestMapping("/api/e14")
@Tag(name = "E14 Pipeline", description = "API para notificar uploads de PDFs E14 y disparar el pipeline de procesamiento")
public class E14NotifyResource {

    private static final Logger LOG = LoggerFactory.getLogger(E14NotifyResource.class);

    private final E14UploadProducer e14UploadProducer;

    public E14NotifyResource(E14UploadProducer e14UploadProducer) {
        this.e14UploadProducer = e14UploadProducer;
    }

    /**
     * Notify that an E14 PDF has been uploaded to MinIO storage.
     * This triggers the OCR processing pipeline.
     */
    @Operation(
        summary = "Notificar upload de PDF E14",
        description = "Después de subir un PDF a MinIO, el frontend llama a este endpoint para notificar al sistema y disparar el pipeline OCR"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Notificación recibida exitosamente",
                content = @Content(schema = @Schema(implementation = Map.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
        }
    )
    @PostMapping("/notify")
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<Map<String, Object>>> notifyUpload(
        @Parameter(description = "Información del archivo subido", required = true) @RequestBody E14UploadNotification notification
    ) {
        LOG.info("REST request to notify E14 upload: fileName={}, storageKey={}", notification.fileName(), notification.storageKey());

        return SecurityUtils.getCurrentUserLogin()
            .defaultIfEmpty("anonymous")
            .map(username -> {
                UUID pdfId = e14UploadProducer.publishUpload(
                    notification.fileName(),
                    notification.storageKey(),
                    notification.electionProcessId(),
                    username
                );

                return ResponseEntity.ok(
                    Map.of(
                        "pdfId",
                        pdfId.toString(),
                        "status",
                        "QUEUED",
                        "message",
                        "E14 PDF queued for processing",
                        "topic",
                        "e14-pdf-uploaded"
                    )
                );
            });
    }

    /**
     * Request body for E14 upload notification.
     */
    public record E14UploadNotification(
        @Schema(description = "Nombre original del archivo", example = "e14_mesa001.pdf") String fileName,
        @Schema(description = "Clave/ruta del archivo en MinIO", example = "e14-pdfs/2024/cali/e14_mesa001.pdf") String storageKey,
        @Schema(description = "ID del proceso electoral asociado", example = "1") Long electionProcessId
    ) {}
}
