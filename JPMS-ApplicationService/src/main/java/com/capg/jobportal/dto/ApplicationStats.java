package com.capg.jobportal.dto;


/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: ApplicationStats
 * DESCRIPTION:
 * This DTO represents aggregated statistics of job applications.
 *
 * It includes counts for:
 * - Total applications
 * - Applied
 * - Under review
 * - Shortlisted
 * - Rejected
 *
 * PURPOSE:
 * Used for analytics and reporting purposes, especially for
 * admin dashboards and internal service communication.
 * ================================================================
 */
public class ApplicationStats {
	
	private long totalApplications;
    private long appliedCount;
    private long underReviewCount;
    private long shortlistedCount;
    private long rejectedCount;


    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long v) { this.totalApplications = v; }

    public long getAppliedCount() { return appliedCount; }
    public void setAppliedCount(long v) { this.appliedCount = v; }

    public long getUnderReviewCount() { return underReviewCount; }
    public void setUnderReviewCount(long v) { this.underReviewCount = v; }

    public long getShortlistedCount() { return shortlistedCount; }
    public void setShortlistedCount(long v) { this.shortlistedCount = v; }

    public long getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(long v) { this.rejectedCount = v; }    
    
}
