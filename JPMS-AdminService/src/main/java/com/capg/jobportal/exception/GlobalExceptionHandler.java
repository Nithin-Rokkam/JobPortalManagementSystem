package com.capg.jobportal.exception;

import feign.FeignException;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.capg.jobportal.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(403, "Forbidden", ex.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(NoFallbackAvailableException.class)
    public ResponseEntity<ErrorResponse> handleCircuitBreaker(NoFallbackAvailableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof FeignException feignEx) {
            int status = feignEx.status() > 0 ? feignEx.status() : 503;
            return ResponseEntity.status(status)
                    .body(new ErrorResponse(status, "Service Error", feignEx.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(503, "Service Unavailable",
                        "Downstream service is unavailable: " + (cause != null ? cause.getMessage() : ex.getMessage())));
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ErrorResponse> handleFeignNotFound(FeignException.NotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Not Found", "The requested resource was not found in the downstream service"));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex) {
        int status = ex.status() > 0 ? ex.status() : 500;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status, "Service Communication Error",
                        "Error communicating with downstream service: " + ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Bad Request", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Internal Server Error", ex.getClass().getSimpleName() + ": " + ex.getMessage()));
    }
}
