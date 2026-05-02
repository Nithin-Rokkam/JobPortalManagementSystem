package com.capg.jobportal.test.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.capg.jobportal.entity.User;
import com.capg.jobportal.enums.Role;
import com.capg.jobportal.enums.UserStatus;

class UserTest {

    @Test
    void gettersAndSetters() {
        User user = new User();
        user.setId(1L);
        user.setName("Test");
        user.setEmail("test@test.com");
        user.setPassword("pass");
        user.setPhone("123");
        user.setRole(Role.JOB_SEEKER);
        user.setStatus(UserStatus.ACTIVE);
        user.setRefreshToken("token");
        user.setProfilePictureUrl("pic.jpg");
        user.setResumeUrl("resume.pdf");

        assertEquals(1L, user.getId());
        assertEquals("Test", user.getName());
        assertEquals("test@test.com", user.getEmail());
        assertEquals("pass", user.getPassword());
        assertEquals("123", user.getPhone());
        assertEquals(Role.JOB_SEEKER, user.getRole());
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertEquals("token", user.getRefreshToken());
        assertEquals("pic.jpg", user.getProfilePictureUrl());
        assertEquals("resume.pdf", user.getResumeUrl());
    }

    @Test
    void fullConstructor() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User(1L, "Test", "test@test.com", "pass", Role.ADMIN, "123",
                UserStatus.ACTIVE, "pic.jpg", "resume.pdf", "token", now, now);
        assertEquals(1L, user.getId());
        assertEquals("Test", user.getName());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    void onCreate_setTimestamps() throws Exception {
        User user = new User();
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());

        Method onCreate = User.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(user);

        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void onUpdate_setsUpdatedAt() throws Exception {
        User user = new User();
        assertNull(user.getUpdatedAt());

        Method onUpdate = User.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(user);

        assertNotNull(user.getUpdatedAt());
    }

    @Test
    void defaultStatus_isActive() {
        User user = new User();
        assertEquals(UserStatus.ACTIVE, user.getStatus());
    }
}
