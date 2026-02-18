package com.tyse.scrutiny.gateway.config;

import reactor.blockhound.BlockHound;
import reactor.blockhound.integration.BlockHoundIntegration;

public class JHipsterBlockHoundIntegration implements BlockHoundIntegration {

    @Override
    public void applyTo(BlockHound.Builder builder) {
        builder.allowBlockingCallsInside("org.springframework.validation.beanvalidation.SpringValidatorAdapter", "validate");
        builder.allowBlockingCallsInside("com.tyse.scrutiny.gateway.security.DomainUserDetailsService", "createSpringSecurityUser");
        builder.allowBlockingCallsInside("org.springframework.web.reactive.result.method.InvocableHandlerMethod", "invoke");
        builder.allowBlockingCallsInside("org.springdoc.core.service.OpenAPIService", "build");
        builder.allowBlockingCallsInside("org.springdoc.core.service.OpenAPIService", "getWebhooks");
        builder.allowBlockingCallsInside("org.springdoc.core.service.AbstractRequestService", "build");
        // Allow springdoc to read files for OpenAPI generation
        builder.allowBlockingCallsInside("org.springdoc.core.providers.SpringDocProviders", "jsonMapper");
        builder.allowBlockingCallsInside("org.springdoc.webflux.api.OpenApiWebfluxResource", "openapiJson");
        builder.allowBlockingCallsInside("com.fasterxml.jackson.databind.ObjectMapper", "writeValueAsString");
        // Allow MessageSource to read .properties files in ExceptionTranslator (i18n error messages)
        builder.allowBlockingCallsInside("org.springframework.context.support.MessageSourceSupport", "getMessage");
        builder.allowBlockingCallsInside("org.springframework.context.support.ResourceBundleMessageSource", "getMessage");
        // Allow ExceptionTranslator to use MessageSource for i18n (calls FileInputStream internally)
        builder.allowBlockingCallsInside("com.tyse.scrutiny.gateway.web.rest.errors.ExceptionTranslator", "customizeProblem");
        // jhipster-needle-blockhound-integration - JHipster will add additional gradle plugins here
    }
}
