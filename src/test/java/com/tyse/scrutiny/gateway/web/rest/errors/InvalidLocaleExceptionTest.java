package com.tyse.scrutiny.gateway.web.rest.errors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Unit tests for {@link InvalidLocaleException}.
 */
class InvalidLocaleExceptionTest {

    @Test
    void testInvalidLocaleExceptionCreation() {
        InvalidLocaleException exception = new InvalidLocaleException();

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void testInvalidLocaleExceptionHasCorrectStatus() {
        InvalidLocaleException exception = new InvalidLocaleException();
        ProblemDetail problemDetail = exception.getBody();

        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void testInvalidLocaleExceptionHasCorrectTitle() {
        InvalidLocaleException exception = new InvalidLocaleException();
        ProblemDetail problemDetail = exception.getBody();

        assertThat(problemDetail.getTitle()).isEqualTo("Invalid language key");
    }

    @Test
    void testInvalidLocaleExceptionHasCorrectDetail() {
        InvalidLocaleException exception = new InvalidLocaleException();
        ProblemDetail problemDetail = exception.getBody();

        assertThat(problemDetail.getDetail()).isEqualTo("Only 'es' and 'en' are supported");
    }

    @Test
    void testInvalidLocaleExceptionHasMessageProperty() {
        InvalidLocaleException exception = new InvalidLocaleException();
        ProblemDetail problemDetail = exception.getBody();

        assertThat(problemDetail.getProperties()).isNotNull();
        assertThat(problemDetail.getProperties()).containsEntry("message", "error.invalidlangkey");
    }

    @Test
    void testInvalidLocaleExceptionHasDefaultType() {
        InvalidLocaleException exception = new InvalidLocaleException();
        ProblemDetail problemDetail = exception.getBody();

        assertThat(problemDetail.getType()).isEqualTo(ErrorConstants.DEFAULT_TYPE);
    }

    @Test
    void testInvalidLocaleExceptionIsErrorResponseException() {
        InvalidLocaleException exception = new InvalidLocaleException();

        assertThat(exception).isInstanceOf(org.springframework.web.ErrorResponseException.class);
    }

    @Test
    void testInvalidLocaleExceptionMessage() {
        InvalidLocaleException exception = new InvalidLocaleException();

        // ErrorResponseException getMessage() returns the detail from ProblemDetail
        String message = exception.getMessage();
        assertThat(message).contains("400");
        assertThat(message).contains("Invalid language key");
    }

    @Test
    void testInvalidLocaleExceptionCauseIsNull() {
        InvalidLocaleException exception = new InvalidLocaleException();

        assertThat(exception.getCause()).isNull();
    }
}
