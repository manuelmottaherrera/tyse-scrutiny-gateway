package com.tyse.scrutiny.gateway.service.authorization.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

/**
 * Unit tests for {@link InactiveAuthorityException}.
 */
class InactiveAuthorityExceptionTest {

    @Test
    void testInactiveAuthorityExceptionCreation() {
        String authorityCode = "ROLE_TEST";
        InactiveAuthorityException exception = new InactiveAuthorityException(authorityCode);

        assertThat(exception).isNotNull();
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getAuthorityCode()).isEqualTo(authorityCode);
    }

    @Test
    void testInactiveAuthorityExceptionHasCorrectStatus() {
        InactiveAuthorityException exception = new InactiveAuthorityException("ROLE_INACTIVE");
        ProblemDetail problemDetail = exception.getBody();

        assertThat(problemDetail).isNotNull();
        assertThat(problemDetail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void testInactiveAuthorityExceptionHasCorrectTitle() {
        InactiveAuthorityException exception = new InactiveAuthorityException("ROLE_DISABLED");
        ProblemDetail problemDetail = exception.getBody();

        assertThat(problemDetail.getTitle()).isEqualTo("Cannot modify inactive authority");
    }

    @Test
    void testInactiveAuthorityExceptionDetailIncludesAuthorityCode() {
        String authorityCode = "ROLE_TEST_INACTIVE";
        InactiveAuthorityException exception = new InactiveAuthorityException(authorityCode);
        ProblemDetail problemDetail = exception.getBody();

        assertThat(problemDetail.getDetail()).isEqualTo("Cannot modify inactive authority: " + authorityCode);
        assertThat(problemDetail.getDetail()).contains(authorityCode);
    }

    @Test
    void testInactiveAuthorityExceptionHasMessageProperty() {
        InactiveAuthorityException exception = new InactiveAuthorityException("ROLE_ANY");
        ProblemDetail problemDetail = exception.getBody();

        assertThat(problemDetail.getProperties()).isNotNull();
        assertThat(problemDetail.getProperties()).containsEntry("message", "error.authority.inactive");
    }

    @Test
    void testInactiveAuthorityExceptionHasAuthorityCodeProperty() {
        String authorityCode = "ROLE_EXAMPLE";
        InactiveAuthorityException exception = new InactiveAuthorityException(authorityCode);
        ProblemDetail problemDetail = exception.getBody();

        assertThat(problemDetail.getProperties()).isNotNull();
        assertThat(problemDetail.getProperties()).containsEntry("authorityCode", authorityCode);
    }

    @Test
    void testInactiveAuthorityExceptionGetAuthorityCode() {
        String authorityCode = "ROLE_CUSTOM";
        InactiveAuthorityException exception = new InactiveAuthorityException(authorityCode);

        assertThat(exception.getAuthorityCode()).isEqualTo(authorityCode);
    }

    @Test
    void testInactiveAuthorityExceptionWithDifferentCodes() {
        String code1 = "ROLE_ADMIN";
        String code2 = "ROLE_USER";

        InactiveAuthorityException exception1 = new InactiveAuthorityException(code1);
        InactiveAuthorityException exception2 = new InactiveAuthorityException(code2);

        assertThat(exception1.getAuthorityCode()).isEqualTo(code1);
        assertThat(exception2.getAuthorityCode()).isEqualTo(code2);
        assertThat(exception1.getBody().getDetail()).contains(code1);
        assertThat(exception2.getBody().getDetail()).contains(code2);
    }

    @Test
    void testInactiveAuthorityExceptionIsErrorResponseException() {
        InactiveAuthorityException exception = new InactiveAuthorityException("ROLE_TEST");

        assertThat(exception).isInstanceOf(org.springframework.web.ErrorResponseException.class);
    }

    @Test
    void testInactiveAuthorityExceptionCauseIsNull() {
        InactiveAuthorityException exception = new InactiveAuthorityException("ROLE_TEST");

        assertThat(exception.getCause()).isNull();
    }

    @Test
    void testInactiveAuthorityExceptionMessage() {
        String authorityCode = "ROLE_SUSPENDED";
        InactiveAuthorityException exception = new InactiveAuthorityException(authorityCode);

        String message = exception.getMessage();
        assertThat(message).contains("400");
        assertThat(message).contains("Cannot modify inactive authority");
    }
}
