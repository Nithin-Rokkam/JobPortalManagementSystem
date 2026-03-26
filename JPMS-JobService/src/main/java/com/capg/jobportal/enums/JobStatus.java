package com.capg.jobportal.enums;


/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * ENUM: JobStatus
 * DESCRIPTION:
 * This enum represents the lifecycle status of a job posting
 * in the system.
 *
 * POSSIBLE VALUES:
 * - ACTIVE  → Job is visible and open for applications
 * - CLOSED  → Job is no longer accepting applications
 * - DRAFT   → Job is created but not yet published
 * - DELETED → Job is soft-deleted and not visible to users
 *
 * PURPOSE:
 * Ensures consistency and type-safety while handling job status
 * across the application and database.
 * ================================================================
 */
public enum JobStatus {
	 	ACTIVE,
	    CLOSED,
	    DRAFT,
	    DELETED
}
