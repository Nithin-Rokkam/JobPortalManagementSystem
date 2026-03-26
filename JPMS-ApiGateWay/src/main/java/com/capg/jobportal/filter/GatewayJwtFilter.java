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


/*
 * ================================================================
 * AUTHOR: Nithin Kuamr Rokkam
 * CLASS: GatewayJwtFilter
 * DESCRIPTION:
 * This is a global filter used in Spring Cloud Gateway to handle
 * authentication and authorization using JWT (JSON Web Token).
 *
 * KEY RESPONSIBILITIES:
 * - Intercepts all incoming API requests
 * - Validates JWT token from Authorization header
 * - Blocks access to internal APIs from external clients
 * - Allows public endpoints without authentication
 * - Extracts user details (userId, role) from token
 * - Passes user context to downstream microservices via headers
 *
 * SECURITY FEATURES:
 * - Prevents unauthorized access
 * - Protects internal endpoints (/internal/**)
 * - Ensures role-based request handling
 *
 * PURPOSE:
 * Acts as a centralized security layer in the API Gateway,
 * ensuring only authenticated and authorized requests are
 * forwarded to microservices.
 * ================================================================
 */
@Component
public class GatewayJwtFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    
    public GatewayJwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    
    /*
     * ================================================================
     * METHOD: filter
     * DESCRIPTION:
     * This method intercepts every incoming request in the API Gateway.
     *
     * FLOW:
     * 1. Extract request path and HTTP method
     * 2. Block access to internal endpoints (/internal/**)
     * 3. Allow public routes without authentication
     * 4. Extract JWT token from Authorization header
     * 5. Validate token using JwtUtil
     * 6. Extract userId and role from token
     * 7. Add user details to request headers (X-User-Id, X-User-Role)
     * 8. Forward request to downstream services
     *
     * SECURITY:
     * - Rejects requests with missing/invalid tokens (401)
     * - Blocks unauthorized internal access (403)
     *
     * RETURNS:
     * - Mono<Void> → reactive response for non-blocking processing
     * ================================================================
     */
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
        
        if (path.contains("/swagger") || path.contains("/v3/api-docs")) {
            return chain.filter(exchange);
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

    
    /*
     * ================================================================
     * METHOD: isPublicRoute
     * DESCRIPTION:
     * Determines whether a given API endpoint is public and does not
     * require authentication.
     *
     * PUBLIC ENDPOINTS:
     * - Authentication APIs (register, login, refresh)
     * - Job browsing APIs (GET /jobs, search, job by id)
     *
     * PURPOSE:
     * Allows unauthenticated users to access specific endpoints
     * while protecting secured APIs.
     *
     * RETURNS:
     * - true  → if endpoint is public
     * - false → if authentication is required
     * ================================================================
     */
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

    /*
     * ================================================================
     * METHOD: onError
     * DESCRIPTION:
     * Handles error responses for unauthorized or forbidden requests.
     *
     * FUNCTIONALITY:
     * - Sets appropriate HTTP status (401 or 403)
     * - Terminates request processing
     *
     * PURPOSE:
     * Provides a consistent way to handle authentication and
     * authorization failures in the gateway.
     *
     * RETURNS:
     * - Mono<Void> → completes the response without forwarding request
     * ================================================================
     */
    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    /*
     * ================================================================
     * METHOD: getOrder
     * DESCRIPTION:
     * Defines the execution priority of this filter in the Gateway
     * filter chain.
     *
     * VALUE:
     * -1 → High priority (executes before most filters)
     *
     * PURPOSE:
     * Ensures authentication logic is applied early before the request
     * reaches downstream services.
     * ================================================================
     */
    @Override
    public int getOrder() {
        return -1;
    }
}