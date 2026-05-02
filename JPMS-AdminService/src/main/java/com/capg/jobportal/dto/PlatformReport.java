package com.capg.jobportal.dto;

import java.util.List;
<<<<<<< HEAD

=======
import lombok.Data;
import lombok.NoArgsConstructor;
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: PlatformReport
 * DESCRIPTION:
<<<<<<< HEAD
 * This DTO represents a comprehensive report of the platform
 * for administrative purposes.
 *
 * It includes:
 * - Total users and total jobs
 * - Application statistics
 * - List of users
 * - List of jobs
 *
 * PURPOSE:
 * Used by Admin Service to generate dashboards and reports
 * providing an overview of platform activity.
 * ================================================================
 */
=======
 * DTO for the comprehensive admin platform report.
 * Lombok generates all boilerplate. Custom constructor kept
 * for the existing call site in AdminService.
 * ================================================================
 */
@Data
@NoArgsConstructor
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
public class PlatformReport {

    private long totalUsers;
    private long totalJobs;
    private ApplicationStats applicationStats;
    private List<UserResponse> users;
    private List<JobResponse> jobs;

<<<<<<< HEAD
    public PlatformReport() {}

    public PlatformReport(long totalUsers, long totalJobs, ApplicationStats applicationStats) {
        this.totalUsers = totalUsers;
        this.totalJobs = totalJobs;
        this.applicationStats = applicationStats;
    }

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalJobs() { return totalJobs; }
    public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }

    public ApplicationStats getApplicationStats() { return applicationStats; }
    public void setApplicationStats(ApplicationStats applicationStats) { this.applicationStats = applicationStats; }

    public List<UserResponse> getUsers() { return users; }
    public void setUsers(List<UserResponse> users) { this.users = users; }

    public List<JobResponse> getJobs() { return jobs; }
    public void setJobs(List<JobResponse> jobs) { this.jobs = jobs; }
=======
    public PlatformReport(long totalUsers, long totalJobs, ApplicationStats applicationStats) {
        this.totalUsers         = totalUsers;
        this.totalJobs          = totalJobs;
        this.applicationStats   = applicationStats;
    }
>>>>>>> c719d7d (Added Frontend(Angular), Lambok, Vitest and updated readme)
}
