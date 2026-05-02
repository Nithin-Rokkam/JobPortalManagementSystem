package com.capg.jobportal.test.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.capg.jobportal.exception.ResourceNotFoundException;
import com.capg.jobportal.exception.UserAlreadyExistsException;

class ExceptionTest {

    @Test
    void resourceNotFoundException_message() {
        ResourceNotFoundException ex = new ResourceNotFoundException("not found");
        assertEquals("not found", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void userAlreadyExistsException_message() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("exists");
        assertEquals("exists", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }
}
