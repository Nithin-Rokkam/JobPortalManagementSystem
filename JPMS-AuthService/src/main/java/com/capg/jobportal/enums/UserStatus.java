package com.capg.jobportal.enums;


/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * ENUM: UserStatus
 * DESCRIPTION:
 * This enum represents the current status of a user account.
 *
 * POSSIBLE VALUES:
 * - ACTIVE   → User account is active and can access the system
 * - INACTIVE → User account exists but is not currently active
 * - BANNED   → User is restricted from accessing the platform
 *
 * PURPOSE:
 * Helps manage user account states and enforce restrictions
 * such as banning or deactivating users.
 * ================================================================
 */
public enum UserStatus {
	ACTIVE,
    INACTIVE,
    BANNED
}
