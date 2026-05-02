package com.capg.jobportal.test.enums;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.capg.jobportal.enums.Role;
import com.capg.jobportal.enums.UserStatus;

class EnumTest {

    @Test
    void role_values() {
        Role[] roles = Role.values();
        assertEquals(3, roles.length);
        assertEquals(Role.JOB_SEEKER, Role.valueOf("JOB_SEEKER"));
        assertEquals(Role.RECRUITER, Role.valueOf("RECRUITER"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }

    @Test
    void userStatus_values() {
        UserStatus[] statuses = UserStatus.values();
        assertEquals(4, statuses.length);
        assertEquals(UserStatus.ACTIVE, UserStatus.valueOf("ACTIVE"));
        assertEquals(UserStatus.INACTIVE, UserStatus.valueOf("INACTIVE"));
        assertEquals(UserStatus.BANNED, UserStatus.valueOf("BANNED"));
        assertEquals(UserStatus.PENDING_VERIFICATION, UserStatus.valueOf("PENDING_VERIFICATION"));
    }
}
