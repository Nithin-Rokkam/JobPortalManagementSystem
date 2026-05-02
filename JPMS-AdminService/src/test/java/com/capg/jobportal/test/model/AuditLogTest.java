package com.capg.jobportal.test.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.capg.jobportal.model.AuditLog;

class AuditLogTest {

    @Test
    void defaultConstructor() {
        AuditLog log = new AuditLog();
        assertNull(log.getId());
        assertNull(log.getAction());
        assertNull(log.getPerformedBy());
        assertNull(log.getDetails());
        assertNull(log.getCreatedAt());
    }

    @Test
    void parameterizedConstructor() {
        AuditLog log = new AuditLog("DELETE_USER", "admin@test.com", "Deleted user 1");
        assertEquals("DELETE_USER", log.getAction());
        assertEquals("admin@test.com", log.getPerformedBy());
        assertEquals("Deleted user 1", log.getDetails());
    }

    @Test
    void gettersAndSetters() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setAction("BAN_USER");
        log.setPerformedBy("admin");
        log.setDetails("Banned user 5");
        LocalDateTime now = LocalDateTime.now();
        log.setCreatedAt(now);

        assertEquals(1L, log.getId());
        assertEquals("BAN_USER", log.getAction());
        assertEquals("admin", log.getPerformedBy());
        assertEquals("Banned user 5", log.getDetails());
        assertEquals(now, log.getCreatedAt());
    }

    @Test
    void onCreate_setsCreatedAt() throws Exception {
        AuditLog log = new AuditLog();
        assertNull(log.getCreatedAt());

        // Use reflection to invoke the protected @PrePersist method
        Method onCreate = AuditLog.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(log);

        assertNotNull(log.getCreatedAt());
    }
}
