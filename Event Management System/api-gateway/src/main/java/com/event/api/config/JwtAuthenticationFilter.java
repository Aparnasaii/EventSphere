package com.event.api.config;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${gateway.public-paths}")
    private String publicPathsConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Allow CORS preflight OPTIONS requests without JWT check
        if (request.getMethod() == HttpMethod.OPTIONS) {
            log.info("CORS preflight request allowed: {}", path);
            return chain.filter(exchange);
        }

        // Allow public paths without a token
        List<String> publicPaths = Arrays.asList(publicPathsConfig.split(","));
        boolean isPublic = publicPaths.stream().anyMatch(path::startsWith);
        if (isPublic) {
            log.info("Public path allowed: {}", path);
            return chain.filter(exchange);
        }

        // Extract Authorization header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", path);
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid JWT token for path: {}", path);
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // Extract claims and forward as trusted headers to downstream services
        Claims claims = jwtUtil.extractAllClaims(token);

        String userId   = claims.get("userId") != null ? claims.get("userId").toString() : "";
        String email    = claims.getSubject() != null ? claims.getSubject() : "";
        String role     = claims.get("role", String.class) != null ? claims.get("role", String.class) : "";

        log.info("JWT validated for path: {} | userId={}, email={}, role={}", path, userId, email, role);

        // Mutate the request — add headers, strip original Authorization if desired
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id",    userId)
                .header("X-User-Email", email)
                .header("X-User-Role",  role)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Run before all other filters
    }
}