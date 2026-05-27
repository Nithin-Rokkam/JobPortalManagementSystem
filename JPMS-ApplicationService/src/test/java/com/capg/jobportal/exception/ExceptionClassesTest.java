package com.capg.jobportal.exception;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ExceptionClassesTest {

    @Test
    void duplicateApplicationException() {
        DuplicateApplicationException ex = new DuplicateApplicationException("dup");
        assertEquals("dup", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void forbiddenException() {
        ForbiddenException ex = new ForbiddenException("forbidden");
        assertEquals("forbidden", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void invalidStatusTransitionException() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException("invalid");
        assertEquals("invalid", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void resourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("not found");
        assertEquals("not found", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }
}
