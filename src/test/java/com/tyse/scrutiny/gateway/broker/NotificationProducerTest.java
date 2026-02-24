package com.tyse.scrutiny.gateway.broker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyse.scrutiny.gateway.domain.User;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String BINDING_NAME = "notificationProducer-out-0";

    @Mock
    private StreamBridge streamBridge;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private NotificationProducer notificationProducer;

    @BeforeEach
    void setUp() {
        notificationProducer = new NotificationProducer(streamBridge, objectMapper);
        ReflectionTestUtils.setField(notificationProducer, "baseUrl", BASE_URL);
    }

    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setLogin("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setLangKey("es");
        user.setActivationKey("abc123");
        user.setResetKey("reset456");
        return user;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> captureNotification() throws Exception {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(streamBridge).send(eq(BINDING_NAME), captor.capture());
        return objectMapper.readValue(captor.getValue(), new TypeReference<>() {});
    }

    @Nested
    class SendActivationEmail {

        @Test
        void shouldBuildActivationUrlWithAccountPrefix() throws Exception {
            User user = createTestUser();

            notificationProducer.sendActivationEmail(user).block();

            Map<String, Object> notification = captureNotification();
            Map<String, Object> templateData = (Map<String, Object>) notification.get("templateData");
            assertThat(templateData.get("activationUrl")).isEqualTo(BASE_URL + "/account/activate?key=" + user.getActivationKey());
        }

        @Test
        void shouldSendAccountActivationType() throws Exception {
            User user = createTestUser();

            notificationProducer.sendActivationEmail(user).block();

            Map<String, Object> notification = captureNotification();
            assertThat(notification.get("type")).isEqualTo("ACCOUNT_ACTIVATION");
            assertThat(notification.get("channel")).isEqualTo("EMAIL");
            assertThat(notification.get("recipient")).isEqualTo(user.getEmail());
        }

        @Test
        void shouldReturnEmptyMonoWhenUserIsNull() {
            Mono<Void> result = notificationProducer.sendActivationEmail(null);

            StepVerifier.create(result).verifyComplete();
            verifyNoInteractions(streamBridge);
        }

        @Test
        void shouldReturnEmptyMonoWhenEmailIsNull() {
            User user = createTestUser();
            user.setEmail(null);

            Mono<Void> result = notificationProducer.sendActivationEmail(user);

            StepVerifier.create(result).verifyComplete();
            verifyNoInteractions(streamBridge);
        }
    }

    @Nested
    class SendCreationEmail {

        @Test
        void shouldBuildCreationUrlWithAccountPrefix() throws Exception {
            User user = createTestUser();

            notificationProducer.sendCreationEmail(user).block();

            Map<String, Object> notification = captureNotification();
            Map<String, Object> templateData = (Map<String, Object>) notification.get("templateData");
            assertThat(templateData.get("activationUrl")).isEqualTo(BASE_URL + "/account/activate?key=" + user.getActivationKey());
        }

        @Test
        void shouldSendUserCreationType() throws Exception {
            User user = createTestUser();

            notificationProducer.sendCreationEmail(user).block();

            Map<String, Object> notification = captureNotification();
            assertThat(notification.get("type")).isEqualTo("USER_CREATION");
        }

        @Test
        void shouldReturnEmptyMonoWhenUserIsNull() {
            Mono<Void> result = notificationProducer.sendCreationEmail(null);

            StepVerifier.create(result).verifyComplete();
            verifyNoInteractions(streamBridge);
        }

        @Test
        void shouldReturnEmptyMonoWhenEmailIsNull() {
            User user = createTestUser();
            user.setEmail(null);

            Mono<Void> result = notificationProducer.sendCreationEmail(user);

            StepVerifier.create(result).verifyComplete();
            verifyNoInteractions(streamBridge);
        }
    }

    @Nested
    class SendPasswordResetMail {

        @Test
        void shouldBuildCorrectPasswordResetUrl() throws Exception {
            User user = createTestUser();

            notificationProducer.sendPasswordResetMail(user).block();

            Map<String, Object> notification = captureNotification();
            Map<String, Object> templateData = (Map<String, Object>) notification.get("templateData");
            assertThat(templateData.get("resetUrl")).isEqualTo(BASE_URL + "/account/reset/finish?key=" + user.getResetKey());
        }

        @Test
        void shouldSendPasswordResetType() throws Exception {
            User user = createTestUser();

            notificationProducer.sendPasswordResetMail(user).block();

            Map<String, Object> notification = captureNotification();
            assertThat(notification.get("type")).isEqualTo("PASSWORD_RESET");
        }

        @Test
        void shouldReturnEmptyMonoWhenUserIsNull() {
            Mono<Void> result = notificationProducer.sendPasswordResetMail(null);

            StepVerifier.create(result).verifyComplete();
            verifyNoInteractions(streamBridge);
        }

        @Test
        void shouldReturnEmptyMonoWhenEmailIsNull() {
            User user = createTestUser();
            user.setEmail(null);

            Mono<Void> result = notificationProducer.sendPasswordResetMail(user);

            StepVerifier.create(result).verifyComplete();
            verifyNoInteractions(streamBridge);
        }
    }

    @Nested
    class CommonBehavior {

        @Test
        void shouldDefaultLocaleToEsWhenLangKeyIsNull() throws Exception {
            User user = createTestUser();
            user.setLangKey(null);

            notificationProducer.sendActivationEmail(user).block();

            Map<String, Object> notification = captureNotification();
            assertThat(notification.get("locale")).isEqualTo("es");
        }

        @Test
        void shouldIncludeUserDataInTemplateData() throws Exception {
            User user = createTestUser();

            notificationProducer.sendActivationEmail(user).block();

            Map<String, Object> notification = captureNotification();
            Map<String, Object> templateData = (Map<String, Object>) notification.get("templateData");
            Map<String, Object> userData = (Map<String, Object>) templateData.get("user");

            assertThat(userData.get("id")).isEqualTo(user.getId().intValue());
            assertThat(userData.get("login")).isEqualTo(user.getLogin());
            assertThat(userData.get("firstName")).isEqualTo(user.getFirstName());
            assertThat(userData.get("lastName")).isEqualTo(user.getLastName());
            assertThat(userData.get("email")).isEqualTo(user.getEmail());
            assertThat(userData.get("langKey")).isEqualTo(user.getLangKey());
        }

        @Test
        void shouldIncludeBaseUrlInTemplateData() throws Exception {
            User user = createTestUser();

            notificationProducer.sendActivationEmail(user).block();

            Map<String, Object> notification = captureNotification();
            Map<String, Object> templateData = (Map<String, Object>) notification.get("templateData");
            assertThat(templateData.get("baseUrl")).isEqualTo(BASE_URL);
        }

        @Test
        void shouldIncludeRequestedAtTimestamp() throws Exception {
            User user = createTestUser();

            notificationProducer.sendActivationEmail(user).block();

            Map<String, Object> notification = captureNotification();
            String requestedAt = (String) notification.get("requestedAt");
            assertThat(requestedAt).isNotNull();
            Instant parsed = Instant.parse(requestedAt);
            assertThat(parsed).isNotNull();
        }
    }
}
