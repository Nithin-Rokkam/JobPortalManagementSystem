package com.capg.jobportal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.capg.jobportal.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(404, "Not Found", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
 
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        ErrorResponse error = new ErrorResponse(403, "Forbidden", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }
 
    @ExceptionHandler(DuplicateApplicationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateApplicationException ex) {
        ErrorResponse error = new ErrorResponse(409, "Conflict", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
 
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex) {
        ErrorResponse error = new ErrorResponse(400, "Bad Request", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
 
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleFileTooLarge(MaxUploadSizeExceededException ex) {
        ErrorResponse error = new ErrorResponse(413, "Payload Too Large", "Resume file must be less than 5MB");
        return new ResponseEntity<>(error, HttpStatus.PAYLOAD_TOO_LARGE);
    }
 
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadInput(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(400, "Bad Request", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
 
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = "";
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            if (!message.isEmpty()) {
                message = message + ", ";
            }
            message = message + fieldError.getDefaultMessage();
        }
        ErrorResponse error = new ErrorResponse(400, "Bad Request", message);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
 
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        ex.printStackTrace();
        ErrorResponse error = new ErrorResponse(500, "Internal Server Error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
