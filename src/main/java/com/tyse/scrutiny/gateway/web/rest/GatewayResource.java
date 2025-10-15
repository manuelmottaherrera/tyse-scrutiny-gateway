package com.tyse.scrutiny.gateway.web.rest;

import com.tyse.scrutiny.gateway.security.AuthoritiesConstants;
import com.tyse.scrutiny.gateway.web.rest.vm.RouteVM;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * REST controller for managing Gateway configuration.
 */
@RestController
@RequestMapping("/api/gateway")
@Tag(name = "Gateway", description = "API para gestionar la configuración del Gateway y rutas de microservicios")
public class GatewayResource {

    private final RouteLocator routeLocator;

    private final DiscoveryClient discoveryClient;

    @Value("${spring.application.name}")
    private String appName;

    public GatewayResource(RouteLocator routeLocator, DiscoveryClient discoveryClient) {
        this.routeLocator = routeLocator;
        this.discoveryClient = discoveryClient;
    }

    /**
     * {@code GET  /routes} : get the active routes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the list of routes.
     */
    @Operation(
        summary = "Obtener rutas activas del Gateway",
        description = "Retorna todas las rutas activas configuradas en el Gateway con sus instancias de servicio descubiertas. Solo accesible para administradores.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Lista de rutas obtenida exitosamente",
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = RouteVM.class)))
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - requiere rol ADMIN", content = @Content),
        }
    )
    @GetMapping("/routes")
    @Secured(AuthoritiesConstants.ADMIN)
    public ResponseEntity<List<RouteVM>> activeRoutes() {
        Flux<Route> routes = routeLocator.getRoutes();
        List<RouteVM> routeVMs = new ArrayList<>();
        routes.subscribe(route -> {
            RouteVM routeVM = new RouteVM();
            // Manipulate strings to make Gateway routes look like Zuul's
            String predicate = route.getPredicate().toString();
            String path = predicate.substring(predicate.indexOf("[") + 1, predicate.indexOf("]"));
            routeVM.setPath(path);
            String serviceId = route.getId().substring(route.getId().indexOf("_") + 1).toLowerCase();
            routeVM.setServiceId(serviceId);
            // Exclude gateway app from routes
            if (!serviceId.equalsIgnoreCase(appName)) {
                routeVM.setServiceInstances(discoveryClient.getInstances(serviceId));
                routeVMs.add(routeVM);
            }
        });
        return ResponseEntity.ok(routeVMs);
    }
}
