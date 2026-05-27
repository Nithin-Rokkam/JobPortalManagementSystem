package com.capg.jobportal.test.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;

import com.capg.jobportal.dto.ErrorResponse;
import com.capg.jobportal.exception.AccessDeniedException;
import com.capg.jobportal.exception.GlobalExceptionHandler;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;

import java.util.Collections;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleAccessDenied() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().getStatus());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void handleMissingHeader() throws NoSuchMethodException {
        MissingRequestHeaderException ex = new MissingRequestHeaderException(
                "X-User-Role",
                new org.springframework.core.MethodParameter(
                        GlobalExceptionHandlerTest.class.getDeclaredMethod("handleMissingHeader"), -1));
        ResponseEntity<ErrorResponse> response = handler.handleMissingHeader(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void handleCircuitBreaker_withFeignCause() {
        Request request = Request.create(Request.HttpMethod.GET, "/test",
                Collections.emptyMap(), null, new RequestTemplate());
        FeignException feignEx = FeignException.errorStatus("test",
                feign.Response.builder().status(404).reason("Not Found")
                        .request(request).headers(Collections.emptyMap()).build());
        NoFallbackAvailableException ex = new NoFallbackAvailableException("fail", feignEx);
        ResponseEntity<ErrorResponse> response = handler.handleCircuitBreaker(ex);
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleCircuitBreaker_withFeignCause_zeroStatus() {
        Request request = Request.create(Request.HttpMethod.GET, "/test",
                Collections.emptyMap(), null, new RequestTemplate());
        FeignException feignEx = new FeignException.ServiceUnavailable("unavail",
                request, null, Collections.emptyMap());
        NoFallbackAvailableException ex = new NoFallbackAvailableException("fail", feignEx);
        ResponseEntity<ErrorResponse> response = handler.handleCircuitBreaker(ex);
        assertNotNull(response.getBody());
    }

    @Test
    void handleCircuitBreaker_withNonFeignCause() {
        RuntimeException cause = new RuntimeException("downstream down");
        NoFallbackAvailableException ex = new NoFallbackAvailableException("fail", cause);
        ResponseEntity<ErrorResponse> response = handler.handleCircuitBreaker(ex);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(503, response.getBody().getStatus());
    }

    @Test
    void handleCircuitBreaker_withNullCause() {
        NoFallbackAvailableException ex = new NoFallbackAvailableException("fail", (Throwable) null);
        ResponseEntity<ErrorResponse> response = handler.handleCircuitBreaker(ex);
        assertEquals(503, response.getBody().getStatus());
    }

    @Test
    void handleFeignNotFound() {
        Request request = Request.create(Request.HttpMethod.GET, "/test",
                Collections.emptyMap(), null, new RequestTemplate());
        FeignException.NotFound ex = new FeignException.NotFound("not found",
                request, null, Collections.emptyMap());
        ResponseEntity<ErrorResponse> response = handler.handleFeignNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleFeignException_withPositiveStatus() {
        Request request = Request.create(Request.HttpMethod.GET, "/test",
                Collections.emptyMap(), null, new RequestTemplate());
        FeignException ex = FeignException.errorStatus("test",
                feign.Response.builder().status(502).reason("Bad Gateway")
                        .request(request).headers(Collections.emptyMap()).build());
        ResponseEntity<ErrorResponse> response = handler.handleFeignException(ex);
        assertEquals(502, response.getBody().getStatus());
    }

    @Test
    void handleFeignException_withZeroStatus() {
        Request request = Request.create(Request.HttpMethod.GET, "/test",
                Collections.emptyMap(), null, new RequestTemplate());
        FeignException ex = new FeignException.ServiceUnavailable("fail",
                request, null, Collections.emptyMap());
        ResponseEntity<ErrorResponse> response = handler.handleFeignException(ex);
        assertNotNull(response.getBody());
    }

    @Test
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("bad input");
        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad input", response.getBody().getMessage());
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("unexpected");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatus());
    }
}
