package com.capg.jobportal.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.core.MethodParameter;

import com.capg.jobportal.dto.ErrorResponse;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("not found");
        ResponseEntity<ErrorResponse> r = handler.handleNotFound(ex);
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
    void handleDuplicate() {
        DuplicateApplicationException ex = new DuplicateApplicationException("duplicate");
        ResponseEntity<ErrorResponse> r = handler.handleDuplicate(ex);
        assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
        assertEquals(409, r.getBody().getStatus());
    }

    @Test
    void handleInvalidTransition() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException("invalid");
        ResponseEntity<ErrorResponse> r = handler.handleInvalidTransition(ex);
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
    }

    @Test
    void handleFileTooLarge() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(5000000);
        ResponseEntity<ErrorResponse> r = handler.handleFileTooLarge(ex);
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, r.getStatusCode());
        assertEquals(413, r.getBody().getStatus());
    }

    @Test
    void handleBadInput() {
        IllegalArgumentException ex = new IllegalArgumentException("bad input");
        ResponseEntity<ErrorResponse> r = handler.handleBadInput(ex);
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
    }

    @Test
    void handleValidation() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "field1", "msg1"));
        bindingResult.addError(new FieldError("target", "field2", "msg2"));

        MethodParameter param = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("handleValidation"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindingResult);

        ResponseEntity<ErrorResponse> r = handler.handleValidation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        assertTrue(r.getBody().getMessage().contains("msg1"));
        assertTrue(r.getBody().getMessage().contains("msg2"));
    }

    @Test
    void handleValidation_singleError() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "field1", "single error"));

        MethodParameter param = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethod("handleValidation_singleError"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindingResult);

        ResponseEntity<ErrorResponse> r = handler.handleValidation(ex);
        assertEquals("single error", r.getBody().getMessage());
    }

    @Test
    void handleGeneral() {
        Exception ex = new Exception("unexpected");
        ResponseEntity<ErrorResponse> r = handler.handleGeneral(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, r.getStatusCode());
        assertEquals(500, r.getBody().getStatus());
    }
}
