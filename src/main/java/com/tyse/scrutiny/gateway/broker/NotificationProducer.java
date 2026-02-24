package com.tyse.scrutiny.gateway.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyse.scrutiny.gateway.domain.User;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Kafka producer for notification-request topic.
 * Replaces direct email sending (MailService) with Kafka messages
 * that are consumed by tyse-scrutiny-micro-notification.
 *
 * All methods are reactive and execute on a bounded elastic scheduler
 * to avoid blocking the reactor event loop.
 */
@Component
public class NotificationProducer {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationProducer.class);
    private static final String BINDING_NAME = "notificationProducer-out-0";

    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    @Value("${jhipster.mail.base-url:http://localhost:8080}")
    private String baseUrl;

    public NotificationProducer(StreamBridge streamBridge, ObjectMapper objectMapper) {
        this.streamBridge = streamBridge;
        this.objectMapper = objectMapper;
    }

    /**
     * Send activation email via notification service.
     *
     * @param user the user to send activation email to
     * @return Mono that completes when the notification is published
     */
    public Mono<Void> sendActivationEmail(User user) {
        if (user == null || user.getEmail() == null) {
            LOG.warn("Cannot send activation email - user or email is null");
            return Mono.empty();
        }

        LOG.debug("Publishing activation notification for user '{}'", user.getEmail());

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("user", mapUserToData(user));
        templateData.put("activationUrl", baseUrl + "/account/activate?key=" + user.getActivationKey());
        templateData.put("baseUrl", baseUrl);

        return publishNotification("ACCOUNT_ACTIVATION", "EMAIL", user.getEmail(), templateData, user.getLangKey());
    }

    /**
     * Send creation email via notification service.
     *
     * @param user the user to send creation email to
     * @return Mono that completes when the notification is published
     */
    public Mono<Void> sendCreationEmail(User user) {
        if (user == null || user.getEmail() == null) {
            LOG.warn("Cannot send creation email - user or email is null");
            return Mono.empty();
        }

        LOG.debug("Publishing creation notification for user '{}'", user.getEmail());

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("user", mapUserToData(user));
        templateData.put("activationUrl", baseUrl + "/account/activate?key=" + user.getActivationKey());
        templateData.put("baseUrl", baseUrl);

        return publishNotification("USER_CREATION", "EMAIL", user.getEmail(), templateData, user.getLangKey());
    }

    /**
     * Send password reset email via notification service.
     *
     * @param user the user to send password reset email to
     * @return Mono that completes when the notification is published
     */
    public Mono<Void> sendPasswordResetMail(User user) {
        if (user == null || user.getEmail() == null) {
            LOG.warn("Cannot send password reset email - user or email is null");
            return Mono.empty();
        }

        LOG.debug("Publishing password reset notification for user '{}'", user.getEmail());

        Map<String, Object> templateData = new HashMap<>();
        templateData.put("user", mapUserToData(user));
        templateData.put("resetUrl", baseUrl + "/account/reset/finish?key=" + user.getResetKey());
        templateData.put("baseUrl", baseUrl);

        return publishNotification("PASSWORD_RESET", "EMAIL", user.getEmail(), templateData, user.getLangKey());
    }

    private Map<String, Object> mapUserToData(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("login", user.getLogin());
        userData.put("firstName", user.getFirstName());
        userData.put("lastName", user.getLastName());
        userData.put("email", user.getEmail());
        userData.put("langKey", user.getLangKey());
        return userData;
    }

    private Mono<Void> publishNotification(String type, String channel, String recipient, Map<String, Object> templateData, String locale) {
        return Mono.fromRunnable(() -> {
            try {
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", type);
                notification.put("channel", channel);
                notification.put("recipient", recipient);
                notification.put("templateData", templateData);
                notification.put("locale", locale != null ? locale : "es");
                notification.put("requestedAt", Instant.now().toString());

                String json = objectMapper.writeValueAsString(notification);
                LOG.info("Publishing notification to Kafka: type={}, recipient={}", type, recipient);
                streamBridge.send(BINDING_NAME, json);
                LOG.debug("Notification published: {}", json);
            } catch (JsonProcessingException e) {
                LOG.error("Failed to serialize notification: {}", e.getMessage(), e);
            }
        })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }
}
