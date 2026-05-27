package com.capg.jobportal.test.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.capg.jobportal.Exceptions.*;
import com.capg.jobportal.dto.ErrorResponse;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("not found");
        ResponseEntity<ErrorResponse> r = handler.handleResourceNotFound(ex);
        assertEquals(HttpStatus.NOT_FOUND, r.getStatusCode());
        assertEquals(404, r.getBody().getStatus());
    }

    @Test
    void handleForbidden() {
        ForbiddenException ex = new ForbiddenException("forbidden");
        ResponseEntity<ErrorResponse> r = handler.handleForbidden(ex);
        assertEquals(HttpStatus.FORBIDDEN, r.getStatusCode());
        assertEquals(403, r.getBody().getStatus());
    }

    @Test
    void handleInvalidJobType() {
        InvalidJobTypeException ex = new InvalidJobTypeException("bad type");
        ResponseEntity<ErrorResponse> r = handler.handleInvalidJobType(ex);
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        assertEquals(400, r.getBody().getStatus());
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("unexpected");
        ResponseEntity<ErrorResponse> r = handler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(500, r.getBody().getStatus());
    }
}
