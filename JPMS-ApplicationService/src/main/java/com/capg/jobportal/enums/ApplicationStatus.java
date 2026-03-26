package com.capg.jobportal.enums;



/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * ENUM: ApplicationStatus
 * DESCRIPTION:
 * This enum represents the different stages of a job application
 * in the recruitment lifecycle.
 *
 * POSSIBLE VALUES:
 * - APPLIED        → Candidate has submitted the application
 * - UNDER_REVIEW   → Application is being reviewed by recruiter
 * - SHORTLISTED    → Candidate has been shortlisted
 * - REJECTED       → Application has been rejected
 *
 * PURPOSE:
 * Helps track and manage the status of job applications,
 * enabling clear communication and workflow between
 * job seekers and recruiters.
 * ================================================================
 */
public enum ApplicationStatus {
    APPLIED,
    UNDER_REVIEW,
    SHORTLISTED,
    REJECTED
}