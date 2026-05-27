package com.capg.jobportal.test.exception;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.capg.jobportal.Exceptions.*;

class ExceptionTest {

    @Test
    void forbiddenException() {
        ForbiddenException ex = new ForbiddenException("forbidden");
        assertEquals("forbidden", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void invalidJobTypeException() {
        InvalidJobTypeException ex = new InvalidJobTypeException("bad type");
        assertEquals("bad type", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void resourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("not found");
        assertEquals("not found", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }
}
