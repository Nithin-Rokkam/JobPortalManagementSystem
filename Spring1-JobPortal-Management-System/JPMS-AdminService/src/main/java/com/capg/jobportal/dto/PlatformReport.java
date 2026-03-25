package com.capg.jobportal.dto;

import java.util.List;

public class PlatformReport {

    private long totalUsers;
    private long totalJobs;
    private ApplicationStats applicationStats;
    private List<UserResponse> users;
    private List<JobResponse> jobs;

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
}
