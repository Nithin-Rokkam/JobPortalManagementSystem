package com.capg.jobportal.enums;


/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * ENUM: Role
 * DESCRIPTION:
 * This enum represents different types of user roles in the system.
 *
 * POSSIBLE VALUES:
 * - JOB_SEEKER → Users who apply for jobs
 * - RECRUITER  → Users who post and manage job listings
 * - ADMIN      → Users with administrative privileges
 *
 * PURPOSE:
 * Provides role-based access control (RBAC) across the application,
 * ensuring proper authorization and feature access for each user type.
 * ================================================================
 */
public enum Role {
	JOB_SEEKER,
    RECRUITER,
    ADMIN
}
