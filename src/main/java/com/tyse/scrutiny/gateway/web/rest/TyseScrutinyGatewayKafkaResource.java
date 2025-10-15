package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.broker.KafkaConsumer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/tyse-scrutiny-gateway-kafka")
@Tag(name = "Kafka", description = "API para publicar y consumir mensajes de Kafka")
public class TyseScrutinyGatewayKafkaResource {

    private static final String PRODUCER_BINDING_NAME = "binding-out-0";

    private static final Logger LOG = LoggerFactory.getLogger(TyseScrutinyGatewayKafkaResource.class);
    private final KafkaConsumer kafkaConsumer;
    private final StreamBridge streamBridge;

    public TyseScrutinyGatewayKafkaResource(StreamBridge streamBridge, KafkaConsumer kafkaConsumer) {
        this.streamBridge = streamBridge;
        this.kafkaConsumer = kafkaConsumer;
    }

    @Operation(summary = "Publicar mensaje en Kafka", description = "Envía un mensaje al topic de Kafka configurado")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204", description = "Mensaje publicado exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error al publicar mensaje", content = @Content),
        }
    )
    @PostMapping("/publish")
    public Mono<ResponseEntity<Void>> publish(
        @Parameter(description = "Mensaje a publicar en Kafka", required = true) @RequestParam("message") String message
    ) {
        LOG.debug("REST request the message : {} to send to Kafka topic", message);
        streamBridge.send(PRODUCER_BINDING_NAME, message);
        return Mono.just(ResponseEntity.noContent().build());
    }

    @Operation(
        summary = "Consumir mensajes de Kafka",
        description = "Retorna un stream reactivo (Server-Sent Events) de mensajes consumidos desde los topics de Kafka"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Stream de mensajes iniciado exitosamente",
                content = @Content(schema = @Schema(type = "string"))
            ),
        }
    )
    @GetMapping("/consume")
    public Flux<String> consume() {
        LOG.debug("REST request to consume records from Kafka topics");
        return this.kafkaConsumer.getFlux();
    }
}
