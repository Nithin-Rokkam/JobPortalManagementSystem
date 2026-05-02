package com.capg.jobportal.dto;

<<<<<<< HEAD

=======
import lombok.Data;
import lombok.NoArgsConstructor;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: ApplicationStats
 * DESCRIPTION:
<<<<<<< HEAD
 * This DTO represents aggregated statistics of job applications
 * across the platform.
 *
 * It includes counts for:
 * - Total applications
 * - Applications in different stages (Applied, Under Review,
 *   Shortlisted, Rejected)
 *
 * PURPOSE:
 * Used for analytics, reporting, and admin dashboards to provide
 * insights into application trends and recruitment progress.
 * ================================================================
 */
=======
 * DTO for aggregated application statistics used in admin reports.
 * Lombok generates all boilerplate.
 * ================================================================
 */
@Data
@NoArgsConstructor
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
public class ApplicationStats {

    private long totalApplications;
    private long appliedCount;
    private long underReviewCount;
    private long shortlistedCount;
    private long rejectedCount;
<<<<<<< HEAD

    public long getTotalApplications() { return totalApplications; }
    public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }

    public long getAppliedCount() { return appliedCount; }
    public void setAppliedCount(long appliedCount) { this.appliedCount = appliedCount; }

    public long getUnderReviewCount() { return underReviewCount; }
    public void setUnderReviewCount(long underReviewCount) { this.underReviewCount = underReviewCount; }

    public long getShortlistedCount() { return shortlistedCount; }
    public void setShortlistedCount(long shortlistedCount) { this.shortlistedCount = shortlistedCount; }

    public long getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; }
=======
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
