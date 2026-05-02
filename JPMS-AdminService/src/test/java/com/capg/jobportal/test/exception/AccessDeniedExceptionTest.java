package com.capg.jobportal.test.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.capg.jobportal.exception.AccessDeniedException;

class AccessDeniedExceptionTest {

    @Test
    void constructor_setsMessage() {
        AccessDeniedException ex = new AccessDeniedException("Not an admin");
        assertEquals("Not an admin", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }
}
