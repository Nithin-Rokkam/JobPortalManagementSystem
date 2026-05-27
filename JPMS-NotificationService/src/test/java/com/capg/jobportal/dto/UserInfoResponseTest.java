package com.capg.jobportal.dto;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class UserInfoResponseTest {

    @Test
    void defaultConstructor() {
        UserInfoResponse u = new UserInfoResponse();
        assertNull(u.getId());
    }

    @Test
    void gettersAndSetters() {
        UserInfoResponse u = new UserInfoResponse();
        u.setId(1L);
        u.setName("Test");
        u.setEmail("test@test.com");

        assertEquals(1L, u.getId());
        assertEquals("Test", u.getName());
        assertEquals("test@test.com", u.getEmail());
    }
}
