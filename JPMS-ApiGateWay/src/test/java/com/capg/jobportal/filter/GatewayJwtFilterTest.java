package com.capg.jobportal.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import com.capg.jobportal.util.JwtUtil;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class GatewayJwtFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain chain;

    private GatewayJwtFilter filter;

    @BeforeEach
    void setUp() {
        filter = new GatewayJwtFilter(jwtUtil);
    }

    @Test
    void getOrder_returnsMinusOne() {
        assertEquals(-1, filter.getOrder());
    }

    @Test
    void filter_internalEndpoint_returnsForbidden() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/internal/users").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_swaggerEndpoint_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/swagger-ui.html").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_apiDocsEndpoint_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/v3/api-docs").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_publicRoute_register_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/register").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_publicRoute_login_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_publicRoute_refresh_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/auth/refresh").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_publicRoute_getJobs_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/jobs").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_publicRoute_searchJobs_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/jobs/search").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_publicRoute_getJobById_passesThrough() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/jobs/123").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
    }

    @Test
    void filter_nonPublicGetRoute_requiresAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/admin/users").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_noAuthHeader_returnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/applications").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_invalidBearerPrefix_returnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/applications")
                .header(HttpHeaders.AUTHORIZATION, "Basic token123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_invalidToken_returnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/applications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.isTokenValid("invalid.token")).thenReturn(false);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_validToken_addsHeadersAndContinues() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/applications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid.token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(jwtUtil.isTokenValid("valid.token")).thenReturn(true);
        when(jwtUtil.extractUserId("valid.token")).thenReturn("42");
        when(jwtUtil.extractRole("valid.token")).thenReturn("JOB_SEEKER");
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        verify(chain).filter(any());
        verify(jwtUtil).extractUserId("valid.token");
        verify(jwtUtil).extractRole("valid.token");
    }

    @Test
    void filter_postJobsNotPublic_requiresAuth() {
        MockServerHttpRequest request = MockServerHttpRequest
                .post("/api/jobs").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }
}
