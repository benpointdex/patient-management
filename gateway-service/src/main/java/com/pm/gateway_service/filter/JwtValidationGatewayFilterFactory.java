package com.pm.gateway_service.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class JwtValidationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationGatewayFilterFactory.class);
    private final WebClient.Builder webClientBuilder;

    public JwtValidationGatewayFilterFactory(WebClient.Builder webClientBuilder) {
        super(Object.class);
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            if (exchange.getRequest().getMethod().name().equals("OPTIONS")) {
                return chain.filter(exchange);
            }
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            log.info("JWT Validation - Auth header present: {}", authHeader != null);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("JWT Validation - Missing or invalid auth header");
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            log.info("JWT Validation - Calling auth service at http://auth-service:4005/validate");
            return webClientBuilder.build()
                    .get()
                    .uri("http://auth-service:4005/validate")
                    .header("Authorization", authHeader)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .flatMap(response -> {
                        log.info("JWT Validation - Auth service response: {}", response);
                        // Resilient Role Extraction: Use 'USER' if 'role' is missing
                        String role = (String) response.getOrDefault("role", "USER");
                        log.info("JWT Validation - Extracted role: {}", role);
                        
                        // Inject the header before passing to downstream services
                        return chain.filter(exchange.mutate()
                                .request(exchange.getRequest().mutate()
                                        .header("X-User-Role", role)
                                        .build())
                                .build());
                    })
                    .onErrorResume(e -> {
                        log.error("JWT Validation - Error during validation: {}", e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }
}
