package com.capg.jobportal.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.capg.jobportal.util.JwtUtil;

import reactor.core.publisher.Mono;

@Component
public class GatewayJwtFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    
    public GatewayJwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        System.out.println("=== FILTER CALLED === Path: " + path + " Method: " + method);

        
     // Block all internal endpoints from external access
        if (path.contains("/internal/")) {
            System.out.println("=== BLOCKING INTERNAL ENDPOINT ===");
            return onError(exchange, HttpStatus.FORBIDDEN);
        }
        
        
        if (isPublicRoute(path, method)) {
            return chain.filter(exchange);
        }
        

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);

        System.out.println("=== GATEWAY DEBUG ===");
        System.out.println("Path: " + path);
        System.out.println("UserId: " + userId);
        System.out.println("Role: " + role);

        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", userId)
                .header("X-User-Role", role)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    private boolean isPublicRoute(String path, String method) {
        if (path.equals("/api/auth/register")) return true;
        if (path.equals("/api/auth/login")) return true;
        if (path.equals("/api/auth/refresh")) return true;

        if ("GET".equalsIgnoreCase(method)) {
            if (path.equals("/api/jobs")) return true;
            if (path.equals("/api/jobs/search")) return true;
            if (path.matches("/api/jobs/\\d+")) return true;
        }

        return false;
    }

    
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    
    @Override
    public int getOrder() {
        return -1;
    }
}