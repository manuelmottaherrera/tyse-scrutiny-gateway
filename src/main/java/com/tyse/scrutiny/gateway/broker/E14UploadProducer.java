package com.tyse.scrutiny.gateway.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

/**
 * Kafka producer for e14-pdf-uploaded topic.
 * Publishes events when E14 PDFs are uploaded to MinIO storage.
 */
@Component
public class E14UploadProducer {

    private static final Logger LOG = LoggerFactory.getLogger(E14UploadProducer.class);
    private static final String BINDING_NAME = "e14UploadProducer-out-0";

    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    public E14UploadProducer(StreamBridge streamBridge, ObjectMapper objectMapper) {
        this.streamBridge = streamBridge;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish an E14 PDF upload event.
     *
     * @param fileName          Original file name
     * @param storageKey        Key/path in MinIO storage
     * @param electionProcessId Associated election process ID
     * @param uploadedBy        User who uploaded the file
     * @return Generated PDF ID
     */
    public UUID publishUpload(String fileName, String storageKey, Long electionProcessId, String uploadedBy) {
        UUID pdfId = UUID.randomUUID();

        try {
            Map<String, Object> event = new HashMap<>();
            event.put("pdfId", pdfId.toString());
            event.put("fileName", fileName);
            event.put("storageUrl", storageKey);
            event.put("electionProcessId", electionProcessId);
            event.put("uploadedBy", uploadedBy);
            event.put("uploadedAt", Instant.now().toString());

            String json = objectMapper.writeValueAsString(event);
            LOG.info("Publishing E14 upload event to Kafka: pdfId={}, fileName={}", pdfId, fileName);
            streamBridge.send(BINDING_NAME, json);
            LOG.debug("E14 upload event published: {}", json);

            return pdfId;
        } catch (JsonProcessingException e) {
            LOG.error("Failed to serialize E14 upload event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish E14 upload event", e);
        }
    }
}
