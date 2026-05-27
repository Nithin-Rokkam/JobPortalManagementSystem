package com.capg.jobportal.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * AUTHOR: Nithin Kumar Rokkam
 * CLASS: PlatformReport
 * DESCRIPTION:
 * DTO for the comprehensive admin platform report.
 * Lombok generates all boilerplate. Custom constructor kept
 * for the existing call site in AdminService.
 * ================================================================
 */
@Data
@NoArgsConstructor
public class PlatformReport {

    private long totalUsers;
    private long totalJobs;
    private ApplicationStats applicationStats;
    private List<UserResponse> users;
    private List<JobResponse> jobs;

    public PlatformReport(long totalUsers, long totalJobs, ApplicationStats applicationStats) {
        this.totalUsers         = totalUsers;
        this.totalJobs          = totalJobs;
        this.applicationStats   = applicationStats;
    }
}
